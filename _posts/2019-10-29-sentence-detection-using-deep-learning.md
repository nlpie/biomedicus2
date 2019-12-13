---
layout: post
title: Sentence Boundary Disambiguation in BioMedICUS
author: Ben Knoll
excerpt: >
  An overview of the deep recurrent neural network model used in BioMedICUS.
---

## Background

Sentence boundary disambiguation or sentence splitting is often the first step in any pipeline and excluding RTF parsing or any other pre-processing that's the case in BioMedICUS as well. The detected Sentence units are then used in most downstream components as a dependency, either to split sequences of tokens for analysis or as a unit of analysis itself.

Despite sentence boundary disambiguation being a mostly "solved" problem in the space of general language, we would often see poor sentence splitting performance on the clinical notes that we process. As sentences are the first analysis component in our pipeline, these errors would often propagate downstream to other analysis. In order to resolve the issues we were having with sentences, we set about developing a new sentence splitter for BioMedICUS.


## Network Architecture

Sentence boundary disambiguation makes sense as a sequence detection / token classification problem, where the first token of any sentence needs to be labelled with a "B" tag and every other token is labelled with an "I" tag. We chose an architecture commonly used for sequence detection, which is the Bidirectional Long Short Term Memory (BI-LSTM).


## Implementation

In order to implement this neural network, we used [PyTorch](https://pytorch.org/).

### Input Mapping

Our input mapping is done in the [InputMapping class](https://github.com/nlpie/biomedicus3/blob/master/python/biomedicus/sentences/input.py). There are two inputs to the model.

#### Character identifiers

The character identifiers are a unique integer identifier assigned to each character, using these identifiers we create vectors for each word from the characters after the previous word and before this word, the characters within the word itself, and the characters after the word and before the next word. Marker identifiers are inserted at boundaries in order to assist the neural network.

```python
def transform_word(self, i, start_of_sequence, text, tokens):
    _, prev_end = tokens[i - 1]
    start, end = tokens[i]
    next_start, _ = tokens[i + 1]
    prior = text[prev_end:start]
    word = text[start:end]
    post = text[end:next_start]
    local_char_ids = self.lookup_char_ids(prior, word, post, start_of_sequence)
    local_word_id = self.lookup_word_id(word)
    return local_char_ids, local_word_id

def lookup_char_ids(self, prior, word, post, start_of_sequence):
    char_ids = ([Vocabulary.BEGIN_SEQUENCE if start_of_sequence else Vocabulary.PREV_TOKEN]
                + [self.char_mapping[c] for c in prior]
                + [Vocabulary.TOKEN_BEGIN]
                + [self.char_mapping[c] for c in word]
                + [Vocabulary.TOKEN_END]
                + [self.char_mapping[c] for c in post]
                + [Vocabulary.NEXT_TOKEN])
    if len(char_ids) > self.word_length:
        return char_ids[:self.word_length]
    elif len(char_ids) < self.word_length:
        padded = [Vocabulary.PADDING for _ in range(self.word_length)]
        padded[:len(char_ids)] = char_ids
        return padded
    else:
        return char_ids
```

These character identifier vectors are padded or cut off to a set length using the padding character identifier: 0. These character identifier vectors make up a matrix where each row is a word, this matrix is padded to a specific size using character ID vectors made entirely of padding IDs. A single sample contains one of these matrices.

#### Word identifiers

Also created is a vector containing a unique identifier for each word looked up from a table of words, with unknown words all mapped to a specific value. The word is lowercased, certain characters are substituted (digits) and are deleted (punctuation). This same substitution was used when training the word embeddings. Words that match a very specific pattern used in our MIMIC training dataset to replace personally identifiable information are entirely substituted for the word 'IDENTIFIER' (capitalized unlike any other words). In the future we may consider doing NER to recognize identifiers prior to sentence detection on text that hasn't been de-identified.

```python
def lookup_word_id(self, word):
    if _identifier.match(word):
        word = 'IDENTIFIER'
    else:
        word = word.lower()
        word = _punct.sub('', word)
        word = _digit.sub('#', word)
    local_word_id = self.word_mapping.get(word, len(self.word_mapping))
    return local_word_id
```

#### Sequence stepping

In order to generate more sample sequences for training, we step through the document token by token looking at windows of a certain size. This ensures that all transitions in the training data are seen by the recurrent neural network and aren't overlooked because they occur at the boundary of mini-batched sequences.

```python
def step_sequence(char_ids, word_ids, labels, sequence_length):
    length = len(labels)
    required_pad = sequence_length - length
    if required_pad > 0:
        yield char_ids, word_ids, labels
    else:
        for i in range(0, length - sequence_length):
            limit = i + sequence_length
            yield char_ids[i:limit], word_ids[i:limit], labels[i:limit]
```

#### Mini-batching

During training we use mini-batching, which is taking multiple sequence examples and batching them together to compute a single gradient at once. This gives us more stability than fully stochastic gradient descent, but makes computing gradients much less intense than doing so for all of our training data at once. Below is the generator used during training to provide batches.

```python
def batches(self, shuffle=True):
    indices = np.arange(len(self.lengths))
    if shuffle:
        np.random.shuffle(indices)
    for batch in range(self.n_batches):
        batch_indices = indices[batch * self.batch_size:(batch + 1) * self.batch_size]
        char_ids = pad_sequence([self.char_ids[idx] for idx in batch_indices], batch_first=True)
        word_ids = pad_sequence([self.word_ids[idx] for idx in batch_indices], batch_first=True)
        labels = pad_sequence([self.labels[idx] for idx in batch_indices], batch_first=True)
        lengths = self.lengths[batch_indices]
        yield (char_ids, word_ids), labels, lengths
```

### BiLSTM

Now that we've covered the Python input layer, we can look at the PyTorch model [here](https://github.com/nlpie/biomedicus3/blob/master/python/biomedicus/sentences/bi_lstm.py). We'll start by looking at the top-level Bi-LSTM model in the ``BiLSTM`` class. The first part is defining all the layers that we will be using in the init method:

```python
  def __init__(self, conf, characters, pretrained):
    super().__init__()

    self.char_cnn = CharCNN(conf, characters)

    pretrained = torch.tensor(pretrained, dtype=torch.float32)
    self.word_embeddings = nn.Embedding.from_pretrained(pretrained, padding_idx=0)

    concatted_word_rep_features = pretrained.shape[1] + conf.char_cnn_output_channels
    self.lstm = nn.LSTM(concatted_word_rep_features,
                        conf.lstm_hidden_size,
                        dropout=conf.dropout,
                        bidirectional=True, batch_first=True)
    self.batch_norm = nn.BatchNorm1d(concatted_word_rep_features)

    # projection from hidden to the "begin of sentence" score
    self.hidden2bos = nn.Linear(2 * conf.lstm_hidden_size, 1)
    self.hparams = vars(conf)
    self.dropout = nn.Dropout(p=conf.dropout)
```

We have
- The character CNN sub-model
- A word embedding lookup against pre-trained word embeddings
- The bi-directional LSTM itself
- A batch normalization to be performed after the concatenation
- A linear projection from the bi-lstm generated contextual word representation to "begin of sentence" logits.
- A dropout layer that gets applied in-between layers.

### Forward pass

After creating the layers that we will be using, the second part of a PyTorch model is the forward pass of the neural network:

```python
def forward(self, chars, words, sequence_lengths):
  assert chars.shape[0] == words.shape[0]
  assert chars.shape[1] == words.shape[1]
  # flatten the batch and sequence dims into words (batch * sequence, word_len, 1)
  word_chars = pack_padded_sequence(chars, sequence_lengths, batch_first=True,
                                    enforce_sorted=False)
  # run the char_cnn on it and then reshape back to [batch, sequence, ...]
  char_pools = PackedSequence(self.char_cnn(word_chars.data), word_chars.batch_sizes,
                              word_chars.sorted_indices, word_chars.unsorted_indices)
  char_pools, _ = pad_packed_sequence(char_pools, batch_first=True)
  char_pools = torch.squeeze(char_pools, -1)

  # Look up the word embeddings
  embeddings = self.word_embeddings(words)
  embeddings = self.dropout(embeddings)

  # Create word representations from the concatenation of the char-cnn derived representation
  # and the word embedding representation
  word_reps = torch.cat((char_pools, embeddings), -1)

  # batch normalization, batch-normalize all words
  word_reps = word_reps.view(-1, word_reps.shape[-1])
  word_reps = self.batch_norm(word_reps)
  word_reps = word_reps.view(embeddings.shape[0], embeddings.shape[1], -1)
  word_reps = self.dropout(word_reps)

  # Run LSTM on the sequences of word representations to create contextual word
  # representations
  packed_word_reps = pack_padded_sequence(word_reps, sequence_lengths,
                                          batch_first=True,
                                          enforce_sorted=False)
  packed_contextual_word_reps, _ = self.lstm(packed_word_reps)
  contextual_word_reps, _ = pad_packed_sequence(packed_contextual_word_reps, batch_first=True)
  # Project to the "begin of sentence" space for each word
  contextual_word_reps = self.dropout(contextual_word_reps)
  return self.hidden2bos(contextual_word_reps)
```

The steps are in order
1. Run the character ids through a CNN using the flattened representation from a PackedSequence, reshape the results back into their padded form.
2. Look up the word embeddings using the word ids.
3. Concatenate these into a combined word representation, perform batch normalization. Batch normalization is a deep learning technique that prevents the mean and variance of representations from wildly fluctuating between batches by normalizing to a distribution computed from a running mean and variance. This provides stability and speeds up training, it gets turned off during inference.
4. Again form the padded combined representation sequences into PackedSequence. PackedSequence is nifty PyTorch tool that accomplishes two things: First, recurrent neural networks like the LSTM used here don't have to spend time iterating over and computing results for pad values. Second, gradients don't flow backwards for pad values. These packed sequences are run through the Bi-LSTM and then unpacked back into padded sequences.
5. Project the contextual word representation to a single "begin of sentence" logit for each word. A logit is the log-odds of a probability $$l = \log\left(\frac{p}{1-p}\right) $$, it's a representation of a probability (domain 0 to 1.0) on the domain of the entire real line ($$-\infty$$ to $$\infty$$) with all positive values mapping to the positive class "1" and all negative values mapping to the negative class "0". The output of a linear projection is naturally in this domain, but how we make it output logits is controlled during training, specifically through our choice of a loss function.


### Character representation of words (CNN)

In addition to the BiLSTM model, there is a submodel for generating representations of words from the characters in those words, which can be seen in the ``CharCNN`` class. Starting with the definition of layers in the init method:

```python
def __init__(self, conf, characters):
    super().__init__()

    self.character_embeddings = nn.Embedding(characters, conf.dim_char, padding_idx=0)
    self.dropout = nn.Dropout(p=conf.dropout)
    self.char_conv = nn.Conv1d(conf.dim_char, conf.char_cnn_output_channels,
                               conf.char_cnn_kernel_size)
```

There are 3 layers here:
- A lookup of 30 dimension learned character embeddings from the character identifiers. This is because the character identifiers are not quantitative, they are categorical. During training we instead learn optimal quantitative representations of characters, each character receives a vector that together form a basis for a vector space representing characters.
- A dropout layer.
- A convolutional layer.

#### CharCNN Network forward pass

```python
def forward(self, words):
    embeddings = self.character_embeddings(words)
    embeddings = self.dropout(embeddings)
    # swap to (word, channels, characters)
    embedding_channels = embeddings.permute(0, 2, 1)
    char_channels = self.char_conv(embedding_channels)
    pools = F.max_pool1d(char_channels, char_channels.shape[-1])
    return pools
```

This performs the following steps:
1. Look up the character embeddings.
2. Randomly dropout a portion of the character embedding values to prevent overfitting to the training data.
3. Run the convolutional layer on the character embeddings. The convolutional layer looks at sequences of 5 character embeddings to learn "shapes". An example of a "shape" might be a newline embedding, followed by the embedding for the start of word marker, followed by an embedding for a capitalized letter, followed by an embedding for an uncapitalized letter, which would probably indicate a higher probability of "begin of sentence". It learns ``conf.char_cnn_output_channels`` such "shapes", creating a vector of values representing how much each subsequence matches the shape we are looking for.
4. Perform max pooling. Max pooling takes the highest value for each shape in all of the subsequences of the word. By doing this, the network becomes invariant to differences in the length of words, or in how many characters occur after the previous word and before this word. So a shape that occurs at characters 6-10 of the word has the same value as it would characters 3-7, which is again why we insert marker characters for the previous word, begin of word, end of word, etc.

## Training

The important part of training can be found in the ``train_on_data`` method of the ``bi_lstm`` module linked above.

```python
# compute the logits for the batch
logits = model(char_ids, word_ids, lengths)

maxlen = labels.shape[1]
mask = torch.arange(maxlen).view(1, -1) < lengths.view(-1, 1)
mask = mask.view(-1)

# compute loss using sequence masking and a special weight for positive labels
loss_fn = torch.nn.BCEWithLogitsLoss(weight=mask, pos_weight=pos_weight)
flat_logits = logits.view(-1)
flat_labels = labels.view(-1).float()
loss = loss_fn(flat_logits, flat_labels)

l1_regularization = 0.01 * torch.norm(torch.cat([x.view(-1) for x in model.hidden2bos.parameters()]))
loss += l1_regularization

loss.backward()
optimizer.step()
```

In this snippet, we perform the following steps.
1. Using the mini-batch input data, compute "begin of sentence" logits for each word.
2. Mask padding values, give "begin of sentence" a weight inversely proportional to its relative frequency.
3. Compute Binary Cross Entropy loss for each of the words, at an l1 regularization penalty, and then propagate backwards through the network.

We give "begin of sentence" a weight inversely proportional to its relative frequency in order so that each begin of sentence example is weighted equivalently to all the other tokens a sentence combined. This is important because our classes are so imbalanced.

To compute the loss, we use binary cross entropy loss, which is defined as:
$$ -(y \log(\hat y) + (1 - y) \log(1 - \hat y)) $$.

In neural network optimization, we minimize a loss function, and minimizing [cross entropy](https://en.wikipedia.org/wiki/Cross_entropy) between our estimate $$\hat y$$ and the label $$y \in \{0, 1\}$$, we are maximizing the likelihood that our probability estimates from our logits ($$ \hat y = \sigma(\text{logit}) = \frac{1}{1 + e^{\text{logit}}}$$) come from the same distribution as our labels. This loss is propagated backwards through the network using auto-differentiation to adjust the weights so that the network accomplishes our goal.

## Conditional Random Fields

The method we use is slightly different than the standard Bi-LSTM-CRF, in that we do not do any Viterbi decoding over the output sequence. First, the sentence splitting problem as formulated here only has two classes, "begin of sentence" and "not begin of sentence", so transition probabilities are of minimal value. Nearly every "1" label will be a transition from "0" and will transition back to "0" afterwards, these probabilities will be built into the linear output projection itself. The part of the CRF we do use however, is the introduction of bi-directional temporal dynamic behavior via the use of the bi-LSTM.

## References

For more information on the BiLSTM network for sequence tagging you can find papers written by Z. Huang, W. Xu, and K. Yu [here](https://arxiv.org/abs/1508.01991) and by X. Ma, and E.H. Hovy [here](https://arxiv.org/abs/1603.01354).

For more information about the training data we used, and problem background you can find our paper and slides presented at MedInfo 2019 and annotation guidelines [here](https://github.com/nlpie/biomedicus3/tree/master/docs/sentences).
