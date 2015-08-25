ALTER TABLE patient ADD assigned_by varchar;
ALTER TABLE patient ADD dob_type varchar;
ALTER TABLE patient ADD active boolean;
ALTER TABLE patient ADD merged_with varchar;
ALTER TABLE patient_update_log ADD event_type varchar;