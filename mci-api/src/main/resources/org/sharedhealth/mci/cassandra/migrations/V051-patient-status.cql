ALTER TABLE patient ADD status varchar;
ALTER TABLE patient ADD date_of_death timestamp;
ALTER TABLE patient DROP is_alive;