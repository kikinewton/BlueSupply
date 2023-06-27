create sequence hibernate_sequence start 1 increment 1;
create table cancelled_request_item (id  serial not null, created_date timestamp, status varchar(10), updated_date timestamp, employee_id int4, request_item_id int4, primary key (id));
create table cancel_payment (id int4 not null, created_date timestamp, last_modified_date timestamp, comment varchar(500), created_by_id int4, last_modified_by_id int4, payment_id int4, primary key (id));
create table department (id  serial not null, created_date timestamp, description varchar(50), name varchar(50), updated_date timestamp, primary key (id));
create table employee (id  serial not null, changed_default_password boolean not null, created_at timestamp, deleted boolean not null, email varchar(50) not null, enabled boolean, first_name varchar(30) not null, full_name varchar(100), last_login timestamp, last_name varchar(30) not null, password varchar(255) not null, phone_no varchar(20) not null, updated_at timestamp, department_id int4, primary key (id));
create table employee_role (employee_id int4 not null, role_id int4 not null);
create table float (id  serial not null, created_date timestamp, estimated_unit_price numeric(19, 2), flagged boolean not null, float_ref varchar(30) not null, item_description varchar(255), quantity int4 not null, updated_date timestamp, created_by_id int4, department_id int4, float_order_id int4, primary key (id));
create table float_grn (id  bigserial not null, approved_by_store_manager boolean not null, created_date timestamp, date_of_approval_by_store_manager timestamp, employee_store_manager int4, float_grn_ref varchar(30), float_order_id int4 not null, status varchar(15), update_date timestamp, created_by_id int4, primary key (id));
create table float_grn_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, float_grn_id int8, primary key (id));
create table float_grn_floats (floatgrn_id int8 not null, floats_id int4 not null, primary key (floatgrn_id, floats_id));
create table float_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, float int4, primary key (id));
create table float_order (id  serial not null, amount numeric(19, 2), approval varchar(20), approval_date timestamp, approved_by int4, auditor_retirement_approval boolean, auditor_retirement_approval_date timestamp, created_date date, deleted boolean not null, description varchar(255), endorsed_by int4, endorsement varchar(20), endorsement_date timestamp, flagged boolean not null, float_order_ref varchar(20), float_type varchar(10), funds_received boolean, gm_retirement_approval boolean, gm_retirement_approval_date timestamp, has_document boolean, requested_by varchar(30), requested_by_email varchar(30), requested_by_phone_no varchar(20), retired boolean not null, retirement_date timestamp, staff_id varchar(20), status varchar(20), created_by_id int4, department_id int4, primary key (id));
create table float_order_supporting_document (float_order_id int4 not null, document_id int4 not null, primary key (float_order_id, document_id));
create table float_payment (id  bigserial not null, amount numeric(19, 2) not null, created_date timestamp, floats_id int4, paid_by_id int4, primary key (id));
create table generated_quote (id int4 not null, created_date timestamp, last_modified_date timestamp, product_description varchar(1000) not null, created_by_id int4, last_modified_by_id int4, supplier_id int4, primary key (id));
create table goods_received_note (id  bigserial not null, approved_by_hod boolean not null, approved_by_store_manager boolean, created_date timestamp, date_of_approval_by_hod timestamp, date_of_approval_store_manager timestamp, employee_hod int4, employee_store_manager int4, grn_ref varchar(50), invoice_amount_payable numeric(19, 2), payment_date timestamp, procurement_manager_id int4, supplier int4 not null, updated_date timestamp, created_by_id int4, invoice_id int4, local_purchase_order_id int4, primary key (id));
create table goods_received_note_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, goods_received_note_id int8, primary key (id));
create table invoice (id int4 not null, created_date timestamp, last_modified_date timestamp, invoice_number varchar(30), created_by_id int4, last_modified_by_id int4, invoice_document_id int4, supplier_id int4, primary key (id));
create table local_purchase_order (id int4 not null, created_date timestamp, last_modified_date timestamp, created_at timestamp, deleted boolean not null, delivery_date timestamp, is_approved boolean, lpo_ref varchar(50), supplier_id int4 not null, updated_date timestamp, created_by_id int4, last_modified_by_id int4, approved_by_id int4, department_id int4, local_purchase_order_draft_id int4, quotation_id int4, primary key (id));
create table local_purchase_order_request_items (local_purchase_order_id int4 not null, request_items_id int4 not null, primary key (local_purchase_order_id, request_items_id));
create table local_purchase_order_draft (id int4 not null, created_date timestamp, last_modified_date timestamp, created_at timestamp, deleted boolean not null, delivery_date timestamp, supplier_id int4 not null, updated_date timestamp, created_by_id int4, last_modified_by_id int4, department_id int4, quotation_id int4, primary key (id));
create table local_purchase_order_draft_request_items (local_purchase_order_draft_id int4 not null, request_items_id int4 not null, primary key (local_purchase_order_draft_id, request_items_id));
create table payment (id int4 not null, created_date timestamp, last_modified_date timestamp, approval_byfmdate timestamp, approval_bygmdate timestamp, approval_from_auditor boolean, approval_fromfm boolean, approval_fromgm boolean, bank varchar(50) not null, cheque_number varchar(30) not null, deleted boolean not null, employee_auditor_id int4, employee_fm_id int4, employee_gm_id int4, payment_amount numeric(19, 2), payment_draft_id int4, payment_method varchar(20), payment_status varchar(20), purchase_number varchar(20), withholding_tax_amount numeric(19, 2) not null, withholding_tax_percentage numeric(19, 2) not null, created_by_id int4, last_modified_by_id int4, goods_received_note_id int8, primary key (id));
create table payment_draft (id  serial not null, approval_by_auditor_date timestamp, approval_byfmdate timestamp, approval_bygmdate timestamp, approval_from_auditor boolean, approval_fromfm boolean, approval_fromgm boolean, bank varchar(50), cheque_number varchar(20), created_date timestamp, deleted boolean not null, employee_auditor_id int4, employee_fm_id int4, employee_gm_id int4, payment_amount numeric(19, 2), payment_method varchar(20), payment_status varchar(20), purchase_number varchar(20) not null, withholding_tax_amount numeric(19, 2) not null, withholding_tax_percentage numeric(19, 2), created_by_id int4, goods_received_note_id int8, primary key (id));
create table payment_draft_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, payment_draft_id int4, primary key (id));
create table payment_schedule (id  serial not null, amount numeric(19, 2), creation_date timestamp, due_date timestamp, updated_date timestamp, supplier_id int4, primary key (id));
create table petty_cash (id  serial not null, amount numeric(19, 2), approval varchar(20), approval_date timestamp, created_date timestamp, deleted boolean not null, endorsement varchar(20), endorsement_date timestamp, name varchar(100) not null, paid boolean not null, petty_cash_ref varchar(20), purpose varchar(20), quantity int4 not null, staff_id varchar(20), status varchar(20), updated_date timestamp, created_by int4, department_id int4, petty_cash_order_id int4, primary key (id));
create table petty_cash_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, petty_cash_id int4, primary key (id));
create table petty_cash_order (id int4 not null, created_date timestamp, last_modified_date timestamp, petty_cash_order_ref varchar(20), requested_by varchar(30), requested_by_phone_no varchar(15), staff_id varchar(15) not null, created_by_id int4, last_modified_by_id int4, primary key (id));
create table petty_cash_order_supporting_document (petty_cash_order_id int4 not null, document_id int4 not null);
create table petty_cash_payment (id  bigserial not null, amount numeric(19, 2) not null, created_date timestamp, paid_by_id int4, petty_cash_id int4, primary key (id));
create table privilege (id  bigserial not null, name varchar(20), primary key (id));
create table quotation (id  serial not null, created_at timestamp, deleted boolean not null, expired boolean not null, linked_to_lpo boolean not null, quotation_ref varchar(30), reviewed boolean not null, employee_id int4, request_document_id int4, supplier_id int4, primary key (id));
create table quotation_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, quotation_id int4, primary key (id));
create table request_item (id  serial not null, approval varchar(50), approval_date timestamp, created_date timestamp, currency varchar(10), deleted boolean not null, endorsement varchar(50), endorsement_date timestamp, name varchar(200) not null, priority_level varchar(25), purpose varchar(200) not null, quantity int4 not null, reason varchar(200) not null, request_date timestamp, request_item_ref varchar(50), request_review varchar(50), request_type varchar(50), status varchar(50), supplied_by int4, total_price numeric(19, 2), unit_price numeric(19, 2), updated_date timestamp, employee_id int4, request_category int4, user_department int4, grn_id int8, primary key (id));
create table request_item_quotations (request_item_id int4 not null, quotation_id int4 not null, primary key (request_item_id, quotation_id));
create table request_item_suppliers (request_id int4 not null, supplier_id int4 not null, primary key (request_id, supplier_id));
create table request_category (id  serial not null, created_date timestamp, description varchar(40), name varchar(20) not null, updated_date timestamp, primary key (id));
create table request_document (id int4 not null, created_date timestamp, last_modified_date timestamp, document_format varchar(20), document_type varchar(10), file_name varchar(120), created_by_id int4, last_modified_by_id int4, primary key (id));
create table request_for_quotation (id  bigserial not null, created_date timestamp, quotation_received boolean not null, updated_date timestamp, supplier_id int4, supplier_request_map_id int4, primary key (id));
create table request_item_comment (id  bigserial not null, created_date timestamp, description varchar(1000), process_with_comment varchar(50), updated_date timestamp, employee_id int4, request_item_id int4, primary key (id));
create table role (id  serial not null, name varchar(30), primary key (id));
create table roles_privileges (role_id int4 not null, privilege_id int8 not null);
create table supplier (id int4 not null, created_date timestamp, last_modified_date timestamp, account_number varchar(20), bank varchar(30), description varchar(50), email varchar(40), location varchar(30), name varchar(50) not null, phone_no varchar(15), registered boolean not null, created_by_id int4, last_modified_by_id int4, primary key (id));
create table supplier_request_map (id  serial not null, created_date timestamp, document_attached boolean not null, updated_date timestamp, request_item_id int4, supplier_id int4, primary key (id));
create table verification_token (id  bigserial not null, created_date timestamp, email varchar(50), expiry_date timestamp, token varchar(50), verification_type varchar(255), primary key (id));
alter table department add constraint UK_1t68827l97cwyxo9r1u6t4p7d unique (name);
alter table employee add constraint UK_fopic1oh5oln2khj8eat6ino0 unique (email);
alter table float_grn_floats add constraint UK_kdyjr04toelxk9j3dy5b1cxs unique (floats_id);
alter table float_payment add constraint uniqueFloatAndAmount unique (floats_id, amount);
alter table local_purchase_order_request_items add constraint UK_6mblv6uqpa4qaawmbij2pjtss unique (request_items_id);
alter table local_purchase_order_draft_request_items add constraint UK_2t88v19algu1t5644s6d3ffw7 unique (request_items_id);
alter table payment add constraint UK_i1uqxyrb8pr5vwb28uykv6bgb unique (cheque_number);
alter table payment add constraint UK_hc1omm63vuyygh1h6dbxlbech unique (purchase_number);
alter table payment_draft add constraint UK_c54qhdarvu6yr47358b222lli unique (purchase_number);
alter table petty_cash_order add constraint UK_rtmrilj8nc8wip2r4kaa4g4lq unique (petty_cash_order_ref);
alter table petty_cash_payment add constraint uniquePettyCashAndAmount unique (petty_cash_id, amount);
alter table request_item add constraint UK_sy4uvy8sggpu9xpjw0ivl5wje unique (request_item_ref);
alter table request_category add constraint UK_93awlg1chascwh4khot2ks496 unique (name);
alter table supplier add constraint UK_c3fclhmodftxk4d0judiafwi3 unique (name);
alter table cancelled_request_item add constraint FK8acn5g6ixsa2xa8klj0qo8nni foreign key (employee_id) references employee;
alter table cancelled_request_item add constraint FKeltqfvbw2hrtq6xtkd9kfowjo foreign key (request_item_id) references request_item;
alter table cancel_payment add constraint FKs62lrhao0rpk4sa54sxkgvmj foreign key (created_by_id) references employee;
alter table cancel_payment add constraint FKk4jd3nyi2dkna80srey3pj8eo foreign key (last_modified_by_id) references employee;
alter table cancel_payment add constraint FKn570ewklg2s6c3irudxnoj1s foreign key (payment_id) references payment;
alter table employee add constraint FKbejtwvg9bxus2mffsm3swj3u9 foreign key (department_id) references department;
alter table employee_role add constraint FK7jol9jrbtlt6ctiehegh6besp foreign key (role_id) references role;
alter table employee_role add constraint FKo7rvk7ejtx29vru9cyhf7o68a foreign key (employee_id) references employee;
alter table float add constraint FKbwovm4yu1cklcwc416gs0tjcn foreign key (created_by_id) references employee;
alter table float add constraint FK2kb57vdf1vo949b8tj6ku4eyc foreign key (department_id) references department;
alter table float add constraint FKqttb5l7oa4rwjw63ct1ew6k5r foreign key (float_order_id) references float_order;
alter table float_grn add constraint FKe59bv4856x2atw99m48b1rmao foreign key (created_by_id) references employee;
alter table float_grn_comment add constraint FKat7oirtcjvfv012qx4numtdov foreign key (employee_id) references employee;
alter table float_grn_comment add constraint FK59vvwe0o4a2h0kj342i261253 foreign key (float_grn_id) references float_grn;
alter table float_grn_floats add constraint FKcpw2vcpyhgdc5mlu8ldu7dwss foreign key (floats_id) references float;
alter table float_grn_floats add constraint FKnxypj306wmef5mbofihfcbd9c foreign key (floatgrn_id) references float_grn;
alter table float_comment add constraint FKllootmnkxcldevxl7e4nw69fg foreign key (employee_id) references employee;
alter table float_comment add constraint FK30yqv92h4456h89n9wc2kebx7 foreign key (float) references float_order;
alter table float_order add constraint FKiehvin0bg64irwq9n8qtn08s3 foreign key (created_by_id) references employee;
alter table float_order add constraint FKn0yxrbuqcd11xo458cc77l554 foreign key (department_id) references department;
alter table float_order_supporting_document add constraint FKn8omvkqu5n0pyefgccn4y22k1 foreign key (document_id) references request_document;
alter table float_order_supporting_document add constraint FKc7imvpk47rf8a0ucpmpxox7u1 foreign key (float_order_id) references float_order;
alter table float_payment add constraint FKcfmad1a43b0stgb58g4nvloyt foreign key (floats_id) references float_order;
alter table float_payment add constraint FKj9du37lwwogwy5fm6abi8htxa foreign key (paid_by_id) references employee;
alter table generated_quote add constraint FKosg5wk2a96wb3qq4m0k5a3chm foreign key (created_by_id) references employee;
alter table generated_quote add constraint FKkgbhmqlcktvxtulyjbv8taevd foreign key (last_modified_by_id) references employee;
alter table generated_quote add constraint FKof81lpscs8hm8rvag4182ttie foreign key (supplier_id) references supplier;
alter table goods_received_note add constraint FKh3fht9fcvn2x2g05bsjnvs2af foreign key (created_by_id) references employee;
alter table goods_received_note add constraint FK8m7kl75lowvrna9hy5dcr6kws foreign key (invoice_id) references invoice;
alter table goods_received_note add constraint FKlrjif7pe48872kjjggs5fk996 foreign key (local_purchase_order_id) references local_purchase_order;
alter table goods_received_note_comment add constraint FKiyvs7yok4oamadhywyojl8c3v foreign key (employee_id) references employee;
alter table goods_received_note_comment add constraint FKfb0j8iymtsutfjl0csrx4wj1i foreign key (goods_received_note_id) references goods_received_note;
alter table invoice add constraint FK4cx2i9m31pqc5oix0d03hkbdm foreign key (created_by_id) references employee;
alter table invoice add constraint FK3hjqvdo5n6ku3tkqgd6iqlgag foreign key (last_modified_by_id) references employee;
alter table invoice add constraint FKf9tf1vtvwl907htdnjwewq1pd foreign key (invoice_document_id) references request_document;
alter table invoice add constraint FKowdq3uqeluk7sryl0iytpj259 foreign key (supplier_id) references supplier;
alter table local_purchase_order add constraint FKgn1mtx0v2toa97q685mx503ee foreign key (created_by_id) references employee;
alter table local_purchase_order add constraint FKhq7acxnewfleqnws7ra4xa4s7 foreign key (last_modified_by_id) references employee;
alter table local_purchase_order add constraint FKi2ts0af9oqq1cbd764590fcn2 foreign key (approved_by_id) references employee;
alter table local_purchase_order add constraint FK7yg1w1k4m0o1tepyjqpkwhwqu foreign key (department_id) references department;
alter table local_purchase_order add constraint FKmvtvy0ndkrgljcmjlwb7np91q foreign key (local_purchase_order_draft_id) references local_purchase_order_draft;
alter table local_purchase_order add constraint FKcr9jrbhcgpmxlasu08k7mdx5s foreign key (quotation_id) references quotation;
alter table local_purchase_order_request_items add constraint FKn87dc64kv4cdjkhygk6ylnnjq foreign key (request_items_id) references request_item;
alter table local_purchase_order_request_items add constraint FKgvxvpd3be7ig75h6xnfqtwjgy foreign key (local_purchase_order_id) references local_purchase_order;
alter table local_purchase_order_draft add constraint FKgd923vw2ggbcshbnu8khyy6s6 foreign key (created_by_id) references employee;
alter table local_purchase_order_draft add constraint FK7kg2i4v751me7t0mhg76ychjv foreign key (last_modified_by_id) references employee;
alter table local_purchase_order_draft add constraint FK5mh06jdkg3j9x6s3rl6o0xn1q foreign key (department_id) references department;
alter table local_purchase_order_draft add constraint FK8c9f3egw5ec2y8dn7h5vd83da foreign key (quotation_id) references quotation;
alter table local_purchase_order_draft_request_items add constraint FKgyvww0m1h7s7m3re2c8f1dpk6 foreign key (request_items_id) references request_item;
alter table local_purchase_order_draft_request_items add constraint FKtao7oogx7uba8s5spxec6utbh foreign key (local_purchase_order_draft_id) references local_purchase_order_draft;
alter table payment add constraint FKm7yr6g9klf3mnetxka8v14btg foreign key (created_by_id) references employee;
alter table payment add constraint FKepu723tanrogmbcdu19ecvn87 foreign key (last_modified_by_id) references employee;
alter table payment add constraint FK7uupmelyewca4ph22ix0fu51q foreign key (goods_received_note_id) references goods_received_note;
alter table payment_draft add constraint FK289hjomkc4kxn5tjsx4ce4gs6 foreign key (created_by_id) references employee;
alter table payment_draft add constraint FKbos3bo5cr1smkj5qo1bqbybpq foreign key (goods_received_note_id) references goods_received_note;
alter table payment_draft_comment add constraint FK1fu2uy9mv0rnc9y7cyuydeep3 foreign key (employee_id) references employee;
alter table payment_draft_comment add constraint FKn15rpkk362tvutwwgfxuk4ix4 foreign key (payment_draft_id) references payment_draft;
alter table payment_schedule add constraint FKjds6o5hr3pgltlxl6w3bqp0y3 foreign key (supplier_id) references supplier;
alter table petty_cash add constraint FKngtfdun13lx2sbn533usl3yo3 foreign key (created_by) references employee;
alter table petty_cash add constraint FKd0scg0j02qigxsjuloe600lxf foreign key (department_id) references department;
alter table petty_cash add constraint FKkjcgjeqyrq4hx9xv22xvkln3x foreign key (petty_cash_order_id) references petty_cash_order;
alter table petty_cash_comment add constraint FKkqnhxw1wg3anh5xj6o2k99518 foreign key (employee_id) references employee;
alter table petty_cash_comment add constraint FKa6c7mlps4petqceq85qamht4b foreign key (petty_cash_id) references petty_cash;
alter table petty_cash_order add constraint FKpuowlgenghwxe1oepmns1eyw1 foreign key (created_by_id) references employee;
alter table petty_cash_order add constraint FK65s8t9615y87e112v1f9u937m foreign key (last_modified_by_id) references employee;
alter table petty_cash_order_supporting_document add constraint FK8eypxjx7jnw0krwkniqmvbpt9 foreign key (document_id) references request_document;
alter table petty_cash_order_supporting_document add constraint FKg8yko5feslix9tcsslw2pts10 foreign key (petty_cash_order_id) references petty_cash_order;
alter table petty_cash_payment add constraint FKpggx57vc1l9s4ruqrxcg0qfj3 foreign key (paid_by_id) references employee;
alter table petty_cash_payment add constraint FKsh97t44hxdhgeaxdu1muy0hwq foreign key (petty_cash_id) references petty_cash;
alter table quotation add constraint FK45h0o7evaf0hyx029t890rwkl foreign key (employee_id) references employee;
alter table quotation add constraint FK4d61g419efai4py3ml9w4tv4a foreign key (request_document_id) references request_document;
alter table quotation add constraint FKar1dy6e0angspildlspwyakwh foreign key (supplier_id) references supplier;
alter table quotation_comment add constraint FK72j4bptjb8t4r2kf7y7emm618 foreign key (employee_id) references employee;
alter table quotation_comment add constraint FKebvxowdpj20vclfmoi4suaank foreign key (quotation_id) references quotation;
alter table request_item add constraint FKkjab8v23iy7dvhq3pu4n85j6x foreign key (employee_id) references employee;
alter table request_item add constraint FKjwmhx4rugmpt1dpcg0w79cf7o foreign key (request_category) references request_category;
alter table request_item add constraint FKf79id9k1cva1snpxgb26k4tlt foreign key (user_department) references department;
alter table request_item add constraint FKa37cdjf58b5wqjaywu4lv5b5i foreign key (grn_id) references goods_received_note;
alter table request_item_quotations add constraint FK9flkf0mx7tg4fmafm0eglb6r foreign key (quotation_id) references quotation;
alter table request_item_quotations add constraint FK8pjfefm3nb5r231lo8gpxo2r7 foreign key (request_item_id) references request_item;
alter table request_item_suppliers add constraint FKq3rmf0rmf8w0jq18agemtp5m2 foreign key (supplier_id) references supplier;
alter table request_item_suppliers add constraint FKpll3slkdxqgjojldwd4h3624j foreign key (request_id) references request_item;
alter table request_document add constraint FKi1v3hpovw4niog34l5k1ld5oo foreign key (created_by_id) references employee;
alter table request_document add constraint FK7dqv6d8ijjgl6i7v7n24x5nn6 foreign key (last_modified_by_id) references employee;
alter table request_for_quotation add constraint FKaru48ethc8mf717j9iinifx5j foreign key (supplier_id) references supplier;
alter table request_for_quotation add constraint FKcb4wko5h6k7ybgmh6xnc8f8w3 foreign key (supplier_request_map_id) references supplier_request_map;
alter table request_item_comment add constraint FKk7ucccclg2j1s0aheetl0j3d5 foreign key (employee_id) references employee;
alter table request_item_comment add constraint FKabi1xdbdkj50qljweeqnrksoh foreign key (request_item_id) references request_item;
alter table roles_privileges add constraint FK5yjwxw2gvfyu76j3rgqwo685u foreign key (privilege_id) references privilege;
alter table roles_privileges add constraint FK9h2vewsqh8luhfq71xokh4who foreign key (role_id) references role;
alter table supplier add constraint FKiv7pwdbe85vqpely06dcp57ny foreign key (created_by_id) references employee;
alter table supplier add constraint FKchj5mb15xuiyjn1vccguu4w0i foreign key (last_modified_by_id) references employee;
alter table supplier_request_map add constraint FKhgjxaj3logikxl5dxj5hmncwe foreign key (request_item_id) references request_item;
alter table supplier_request_map add constraint FKs4qtoq2172wfk68cwl0mcbcsa foreign key (supplier_id) references supplier;
alter table store add constraint UK_store_name_u940 unique (name);


CREATE TABLE store ( id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL, created_by_id INTEGER, created_date TIMESTAMP WITHOUT TIME ZONE, last_modified_by_id INTEGER, last_modified_date TIMESTAMP WITHOUT TIME ZONE, name VARCHAR(50), CONSTRAINT pk_store PRIMARY KEY (id) );

ALTER TABLE request_item ADD COLUMN receiving_store_id INTEGER;
ALTER TABLE request_item ADD CONSTRAINT FK_REQUEST_ITEM_ON_RECEIVING_STORE FOREIGN KEY (receiving_store_id) REFERENCES store (id);


CREATE OR REPLACE VIEW public.float_aging_analysis as SELECT f.float_order_ref AS float_ref, upper(f.staff_id::text) AS staff_id, upper(f.description::text) AS item_description, f.amount AS estimated_amount, ( SELECT upper(d.name::text) AS upper FROM department d WHERE d.id = f.department_id) AS department, ( SELECT upper(e.full_name::text) AS upper FROM employee e WHERE e.id = f.created_by_id) AS employee, upper(f.requested_by::text) AS requested_by, upper(f.requested_by_phone_no::text) AS requested_by_phone_no, upper(f.requested_by_email::text) AS requested_by_email, f.created_date, ( SELECT CURRENT_DATE - f.created_date) AS ageing_value FROM float_order f where f.deleted = false and f.retired = false;
CREATE OR REPLACE VIEW public.float_payment_report AS WITH cte_emp AS ( SELECT e.id, e.full_name FROM employee e ), cte_fp AS ( SELECT fp.id, fp.amount, fp.created_date, fp.floats_id, fp.paid_by_id FROM float_payment fp ), cte_flo AS ( SELECT fo_1.id, fo_1.amount, fo_1.approval, fo_1.approval_date, fo_1.auditor_retirement_approval, fo_1.auditor_retirement_approval_date, fo_1.created_date, fo_1.deleted, fo_1.description, fo_1.endorsement, fo_1.endorsement_date, fo_1.flagged, fo_1.float_order_ref, fo_1.funds_received, fo_1.gm_retirement_approval, fo_1.gm_retirement_approval_date, fo_1.has_document, fo_1.requested_by, fo_1.requested_by_email, fo_1.requested_by_phone_no, fo_1.retired, fo_1.retirement_date, fo_1.staff_id, fo_1.status, fo_1.created_by_id, fo_1.department_id, fo_1.approved_by, fo_1.endorsed_by, fo_1.float_type FROM float_order fo_1 ) SELECT fo.id, fo.float_order_ref, fo.amount AS requested_amount, fo.float_type, cte_fp.amount AS paid_amount, fo.created_date AS requested_date, fo.staff_id AS requester_staff_id, fo.requested_by, ( SELECT cte_emp.full_name FROM cte_emp WHERE cte_emp.id = fo.created_by_id) AS created_by, ( SELECT d.name FROM department d WHERE d.id = fo.department_id) AS department, fo.endorsement_date, ( SELECT COALESCE(e.full_name, ''::character varying) AS "coalesce" FROM cte_emp e WHERE e.id = fo.endorsed_by) AS endorsed_by, fo.approval_date, ( SELECT COALESCE(e.full_name, ''::character varying) AS "coalesce" FROM cte_emp e WHERE e.id = fo.approved_by) AS approved_by, cte_fp.created_date AS funds_allocated_date, ( SELECT e.full_name FROM employee e WHERE e.id = cte_fp.paid_by_id) AS account_officer, fo.retirement_date, fo.retired FROM cte_flo fo JOIN cte_fp ON fo.id = cte_fp.floats_id;
CREATE OR REPLACE VIEW public.grn_report AS SELECT grn.id, grn.grn_ref, ( SELECT e.full_name FROM employee e WHERE e.id = grn.created_by_id) AS issuer, grn.approved_by_hod, grn.invoice_amount_payable, ( SELECT ri.name FROM request_item ri WHERE ri.id = lpori.request_items_id) AS request_item, ( SELECT s.name FROM supplier s WHERE s.id = grn.supplier) AS supplier, ( SELECT i.invoice_number FROM invoice i WHERE i.id = grn.invoice_id) AS invoice_number, date(grn.created_date) AS date_received FROM goods_received_note grn JOIN local_purchase_order_request_items lpori ON grn.local_purchase_order_id = lpori.local_purchase_order_id ORDER BY grn.created_date DESC;
CREATE OR REPLACE VIEW public.payment_report AS select p.id, ( select s.name from supplier s where s.id = grn.supplier) as supplier, ( select i.invoice_number from invoice i where i.id = grn.invoice_id) as invoice_no, ( select s.account_number from supplier s where s.id = grn.supplier) as account_number, ( select e.full_name from employee e where e.id = p.employee_gm_id) as approved_by_gm, ( select e.full_name from employee e where e.id = p.employee_fm_id) as verified_by_fm, ( select e.full_name from employee e where e.id = p.employee_auditor_id) as checked_by_auditor, p.cheque_number, p.purchase_number, date(grn.payment_date) as payment_due_date, p.payment_amount as payable_amount, p.withholding_tax_percentage, p.withholding_tax_amount, p.payment_status, date(p.created_date) as payment_date from payment p join goods_received_note grn on p.goods_received_note_id = grn.id;
CREATE OR REPLACE VIEW public.petty_cash_payment_report AS WITH pc_cte AS ( SELECT pc.id, pc.amount, pc.approval, pc.approval_date, pc.created_date, pc.endorsement, pc.endorsement_date, pc.name, pc.paid, pc.petty_cash_ref, pc.purpose, pc.quantity, pc.status, pc.updated_date, pc.created_by, pc.department_id, pc.petty_cash_order_id FROM petty_cash pc WHERE deleted = false ), emp_cte AS ( SELECT e.id, e.created_at, e.email, e.enabled, e.first_name, e.full_name, e.last_login, e.last_name, e.password, e.phone_no, e.updated_at, e.department_id, e.changed_default_password FROM employee e ), dep_cte AS ( SELECT d2.id, d2.created_date, d2.description, d2.name, d2.updated_date FROM department d2 ), pyt_cte AS ( SELECT pcp.id, pcp.amount, pcp.created_date, pcp.paid_by_id, pcp.petty_cash_id FROM petty_cash_payment pcp ) SELECT date(pyt_cte.created_date) AS payment_date, pc_cte.name AS petty_cash_description, pc_cte.petty_cash_ref, pc_cte.purpose, pc_cte.quantity, pc_cte.amount, pc_cte.quantity::numeric * pc_cte.amount AS total_cost, emp_cte.full_name AS requested_by, emp_cte.email AS requested_by_email, dep_cte.name AS department, ( SELECT emp_cte_1.full_name FROM emp_cte emp_cte_1 WHERE emp_cte_1.id = pyt_cte.paid_by_id) AS paid_by FROM pc_cte JOIN emp_cte ON pc_cte.created_by = emp_cte.id JOIN dep_cte ON pc_cte.department_id = dep_cte.id JOIN pyt_cte ON pc_cte.id = pyt_cte.petty_cash_id;
CREATE OR REPLACE VIEW public.procured_item_report AS SELECT ri.id, ri.request_item_ref, ri.name, ri.reason, ri.purpose, ri.quantity, ri.total_price, ri.created_date, ( SELECT e.full_name FROM employee e WHERE e.id = ri.employee_id) AS requested_by, ( SELECT e.email FROM employee e WHERE e.id = ri.employee_id) AS requested_by_email, grn.created_date AS grn_issued_date, ( SELECT d.name FROM department d WHERE d.id = ri.user_department) AS user_department, ( SELECT rc.name FROM request_category rc WHERE rc.id = ri.request_category) AS category, ( SELECT s.name FROM supplier s WHERE s.id = ri.supplied_by) AS supplied_by FROM request_item ri JOIN local_purchase_order_request_items lpori ON lpori.request_items_id = ri.id JOIN goods_received_note grn ON grn.local_purchase_order_id = lpori.local_purchase_order_id where ri.deleted = false;
CREATE OR REPLACE VIEW public.request_per_current_month_per_department AS SELECT d.id, d.name AS department, count(r.id) AS num_of_request FROM department d JOIN employee e ON e.department_id = d.id JOIN request_item r ON r.employee_id = e.id WHERE r.deleted = false and date_part('month'::text, r.created_date) = date_part('month'::text, CURRENT_DATE) GROUP BY d.name, d.id;


INSERT INTO public.role VALUES (1, 'ROLE_ADMIN');
INSERT INTO public.role VALUES (2, 'ROLE_REGULAR');
INSERT INTO public.role VALUES (3, 'ROLE_HOD');
INSERT INTO public.role VALUES (4, 'ROLE_GENERAL_MANAGER');
INSERT INTO public.role VALUES (5, 'ROLE_PROCUREMENT_OFFICER');
INSERT INTO public.role VALUES (6, 'ROLE_STORE_OFFICER');
INSERT INTO public.role VALUES (7, 'ROLE_ACCOUNT_OFFICER');
INSERT INTO public.role VALUES (8, 'ROLE_CHIEF_ACCOUNT_OFFICER');
INSERT INTO public.role VALUES (9, 'ROLE_PROCUREMENT_MANAGER');
INSERT INTO public.role VALUES (10, 'ROLE_FINANCIAL_MANAGER');
INSERT INTO public.role VALUES (11, 'ROLE_AUDITOR');
INSERT INTO public.role VALUES (12, 'ROLE_STORE_MANAGER');

INSERT INTO public.privilege VALUES (3, 'READ_PRIVILEGE');
INSERT INTO public.privilege VALUES (4, 'WRITE_PRIVILEGE');

INSERT INTO public.roles_privileges VALUES (1, 3);
INSERT INTO public.roles_privileges VALUES (1, 4);
INSERT INTO public.roles_privileges VALUES (2, 3);
INSERT INTO public.roles_privileges VALUES (2, 4);
INSERT INTO public.roles_privileges VALUES (3, 3);
INSERT INTO public.roles_privileges VALUES (3, 4);
INSERT INTO public.roles_privileges VALUES (4, 3);
INSERT INTO public.roles_privileges VALUES (4, 4);
INSERT INTO public.roles_privileges VALUES (5, 3);
INSERT INTO public.roles_privileges VALUES (5, 4);
INSERT INTO public.roles_privileges VALUES (6, 3);
INSERT INTO public.roles_privileges VALUES (6, 4);
INSERT INTO public.roles_privileges VALUES (7, 3);
INSERT INTO public.roles_privileges VALUES (7, 4);
INSERT INTO public.roles_privileges VALUES (8, 3);
INSERT INTO public.roles_privileges VALUES (8, 4);
INSERT INTO public.roles_privileges VALUES (9, 3);
INSERT INTO public.roles_privileges VALUES (9, 4);
INSERT INTO public.roles_privileges VALUES (10, 3);
INSERT INTO public.roles_privileges VALUES (10, 4);
INSERT INTO public.roles_privileges VALUES (11, 3);
INSERT INTO public.roles_privileges VALUES (11, 4);

INSERT INTO department (id, name, description) VALUES (10, 'Culinary', 'Catering for employees');
INSERT INTO department (id, name, description) VALUES (11, 'IT', 'IT department');


INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(1, false, '2023-06-11 09:30:59.179', false, 'derrickagyemang12@outlook.com', false, 'Super', 'Super Admin', NULL, 'Admin', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '000000000000', '2023-06-11 09:31:14.899', 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(2, false, '2023-06-11 09:30:59.179', false, 'kikinewton@gmail.com', true, 'Kiki', 'Newton', NULL, 'Admin', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '00000000061', '2023-06-11 09:31:14.899', 10);

INSERT INTO employee (id, changed_default_password, created_at, deleted, email, enabled, first_name, full_name, last_login, last_name, "password", phone_no, updated_at, department_id)
VALUES(3, false, '2023-06-11 09:30:59.179', false, 'chulk@mail.com', true, 'Mark', 'Mark Freeman', NULL, 'Freeman', '$2a$10$0YozPuPfeu2pYK5jUEq7Outf.240hM.j/ny.kdyRiNqoAIXP2FRKG', '000000000081', '2023-06-11 09:31:14.899', 10);

INSERT INTO public.employee_role (employee_id, role_id) VALUES(3, 3);

INSERT INTO public.store (id, created_by_id, created_date, last_modified_by_id, last_modified_date, "name")
VALUES(100, 1, NOW(), NULL, NULL, 'Engineering store');

INSERT INTO public.verification_token (created_date, email, expiry_date, "token", verification_type) VALUES (NOW(), 'kikinewton@gmail.com', NOW() + INTERVAL '1 day', 'c2d297-3d0bKd497', 'PASSWORD_RESET');

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(100, 'PENDING', NULL, NOW(), NULL, false, 'PENDING', NULL, '1 BUCKET OF RED OXIDE PAINT', 'NORMAL', 'SITE DRAIN COVERS', 1, 'FreshNeed', '2022-12-09 15:26:05.331', 'RQI-TRA-00000314-912', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, NOW(), 1, NULL, 11, NULL, 100);

INSERT INTO public.request_item (id, approval, approval_date, created_date, currency, deleted, endorsement, endorsement_date, "name", priority_level, purpose, quantity, reason, request_date, request_item_ref, request_review, request_type, status, supplied_by, total_price, unit_price, updated_date, employee_id, request_category, user_department, grn_id, receiving_store_id)
VALUES(101, 'PENDING', NULL, '2022-11-15 13:15:31.108', NULL, false, 'ENDORSED', '2022-11-15 15:35:24.016', 'TILT COUPLER ([FEMALE AND MALE)', 'NORMAL', 'GR 8053-14 AND GR 7006-18', 4, 'Replace', '2022-11-15 13:15:31.107', 'RQI-TRA-00000110-1511', NULL, 'GOODS_REQUEST', 'PENDING', NULL, 0.00, 0.00, '2022-11-18 11:56:59.721', 1, NULL, 11, NULL, 100);

INSERT INTO public.supplier (id, created_date, last_modified_date, account_number, bank, description, email, "location", "name", phone_no, registered, created_by_id, last_modified_by_id)
VALUES(1, NOW(), NOW(), NULL, NULL, 'IT Equipments', NULL, NULL, 'Jilorm Ventures', '0000000000', false, 1, 1);

INSERT INTO public.supplier
(id, created_date, last_modified_date, account_number, bank, description, email, "location", "name", phone_no, registered, created_by_id, last_modified_by_id)
VALUES(2, NOW(), NOW(), NULL, NULL, 'Internet Service Provider', 'rand19@mail.com', 'Accra', 'Ginet Technology Limited', '88377288192', true, 1, 1);


INSERT INTO public.request_item_suppliers
(request_id, supplier_id)
VALUES(101, 1);

