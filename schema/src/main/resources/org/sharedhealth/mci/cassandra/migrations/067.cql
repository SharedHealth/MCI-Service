ALTER TABLE patient_update_log ADD requested_by varchar;
ALTER TABLE patient_update_log ADD approved_by varchar;

ALTER TABLE patient_audit_log ADD requested_by varchar;
ALTER TABLE patient_audit_log ADD approved_by varchar;