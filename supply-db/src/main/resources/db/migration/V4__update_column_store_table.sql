ALTER TABLE store RENAME COLUMN created_by TO created_by_id;
ALTER TABLE store RENAME COLUMN last_modified_by TO last_modified_by_id;
alter table store add constraint FKixs2wmnld5ldfxk0q27od0v6j foreign key (created_by_id) references employee;
alter table store add constraint FKf18jgat64phjo4ms2oy5fennm foreign key (last_modified_by_id) references employee;

