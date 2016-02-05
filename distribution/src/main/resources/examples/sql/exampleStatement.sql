select b.note_id as "note_id",
        meta.pat_mrn as "pat_mrn",
        meta.service_date as "service_date",
        meta.filing_date as "filing_date",
        meta.prov_id as "prov_id",
        meta.prov_type as "prov_type",
        meta.prov_name as "prov_name",
        meta.encounter_id as "encounter_id",
        meta.encounter_dept as "encounter_dept",
        meta.encounter_dept_specialty as "encounter_dept_specialty",
        meta.encounter_center as "encounter_center",
        meta.encounter_center_type as "encounter_center_type",
        meta.encounter_clinic_type as "encounter_clinic_type",
        meta.kod as "kod",
        meta.tos as "tos",
        meta.setting as "setting",
        meta.smd as "smd",
        meta.role as "role",
        note.note_text as "note_text",
        note.note_id as "note_csn_id"
from
(select *
  from
( select rownum rnum, a.*
    from (select note_id from nlp_sdbx_demo_note_meta order by note_id) a
   where rownum <= 1000 )
where rnum >= 1) b
join nlp_sdbx_demo_note_meta meta on b.note_id = meta.note_id
join nlp_sdbx_all_notes n_all on meta.note_id = n_all.note_id
join rdc_dt.dtv_notes note on n_all.note_csn_id = note.note_id