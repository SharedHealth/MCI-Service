CREATE TABLE IF NOT EXISTS patient_audit_log (
    health_id varchar,
    event_id timeuuid,
    change_set varchar,
    PRIMARY KEY (health_id, event_id)
) WITH CLUSTERING ORDER BY (event_id ASC);

INSERT INTO patient_audit_log (health_id, event_id, change_set) VALUES ('h100', 6a3c7790-bb46-11e4-bf4d-178a0e65aecd,
'[
	{
	    "field_name": "given_name",
		"proposed_by": "x",
		"approved_by": "y",
		"old_value": "monika",
		"new_value": "monica"
	},

	{
	    "field_name": "edu_level",
		"proposed_by": "p",
		"approved_by": "q",
		"old_value": "10",
		"new_value": "11"
	},

	{
	    "field_name": "occupation",
		"proposed_by": "a",
		"approved_by": "b",
		"old_value": "20",
		"new_value": "21"
	}
]');

INSERT INTO patient_audit_log (health_id, event_id, change_set) VALUES ('h100', ed9e6390-bb47-11e4-8e51-178a0e65aecd,
'[
	{
	    "field_name": "religion",
		"proposed_by": "x",
		"approved_by": "y",
		"old_value": "00",
		"new_value": "01"
	}
]');

INSERT INTO patient_audit_log (health_id, event_id, change_set) VALUES ('h100', 2755a080-bb48-11e4-b661-178a0e65aecd,
'[
	{
	    "field_name": "edu_level",
		"proposed_by": "p",
		"approved_by": "q",
		"old_value": "11",
		"new_value": "12"
	},

	{
	    "field_name": "occupation",
		"proposed_by": "a",
		"approved_by": "b",
		"old_value": "21",
		"new_value": "22"
	}
]');
