package com.logistics.supply.util;

public class Constants {
  public static final String tableHeader = "<th>%s</th>";
  public static final String tableData = "<td>%s</td>";
  public static final String tableRow = "<tr>%s</tr>";

  public static final String SUCCESS = "SUCCESS";
  public static final String ERROR = "ERROR";

  public static final String EMPLOYEE_REQUEST_ENDORSED_MAIL =
      "Kindly note that your request has been endorsed by your HOD";
  public static final String HOD_REVIEW_MAIL =
      "Kindly note that quotation for an endorsed request has been sent to you";
  public static final String EMPLOYEE_FLOAT_ENDORSED_MAIL =
      "Kindly note that your request has been endorsed by your HOD";
  public static final String REQUEST_GM_APPROVAL_OF_FLOAT =
      "Kindly login to view endorsed Floats pending approval";
  public static final String REQUEST_QUOTATION_FROM_PROCUREMENT_MAIL =
      "Kindly click on the link below to view quotations from suppliers %s :\n";
    public static final String REQUEST_PENDING_APPROVAL_TITLE = "REQUEST PENDING APPROVAL";
  public static final String REQUEST_PENDING_ENDORSEMENT_TITLE = "REQUEST PENDING ENDORSEMENT";
  public static final String REQUEST_APPROVAL_MAIL_TO_EMPLOYEE =
      "Kindly note that your request item(s) listed below have been approved";

  public static final String REQUEST_CANCELLED_MAIL_TO_EMPLOYEE =
      "Kindly note that your request item(s) listed below have been cancelled";

  public static final String GOODS_RECEIVED_MESSAGE =
      "Kindly note that stores has received these goods";

  public static final String[] procured_items_header = {
    "id",
    "request_item_ref",
    "name",
    "reason",
    "purpose",
    "quantity",
    "total_price",
    "created_date",
    "requested_by",
    "requested_by_email",
    "grn_issued_date",
    "user_department",
    "request_category",
    "supplied_by"
  };

  public static final String[] payment_report_header = {
    "id",
    "supplier",
    "invoice_no",
    "account_number",
    "approved_by_gm",
    "verified_by_fm",
    "checked_by_auditor",
    "cheque_number",
    "purchase_number",
    "payment_due_date",
    "paid_amount",
    "withholding_tax_percentage",
    "withholding_tax_amount",
    "payment_status",
    "payment_date",
  };

  public static final String[] grn_report_header = {
    "id",
    "grnRef",
    "issuer",
    "approved_by_hod",
    "invoice_amount_payable",
    "request_item",
    "supplier",
    "invoice_number",
    "date_received"
  };

  public static final String[] petty_cash_payment_header = {
    "payment_date",
    "petty_cash_description",
    "petty_cash_ref",
    "purpose",
    "quantity",
    "amount",
    "total_cost",
    "staff_id",
    "requested_by",
    "requested_by_email",
    "department",
    "paid_by"
  };

  public static final String[] float_ageing_report_header = {
    "float_ref",
    "staff_id",
    "description",
    "estimated_amount",
    "department",
    "employee",
    "requested_by",
    "requested_by_phone_no",
    "requested_by_email",
    "created_date",
    "ageing_value"
  };

  public static final String[] float_order_payment_report_header = {
    "id",
    "float_order_ref",
    "requested_amount",
    "float_type",
    "paid_amount",
    "requested_date",
    "requester_staff_id",
    "requested_by",
    "created_by",
    "department",
    "endorsement_date",
    "endorsed_by",
    "approval_date",
    "approved_by",
    "funds_allocated_date",
    "account_officer",
    "retired",
    "retirement_date"
  };

  public static final String[] comment_header = {
    "comment_id", "reference", "description", "created_date", "process", "comment_by"
  };

  public static final String payment_due_reminder_message =
      "Please note that the payment for suppliers listed below will be due in 7 days or less";
  public static final String float_aging_analysis_query =
      "select f.float_ref as float_ref, f.item_description as item_description, f.quantity as quantity, f.estimated_unit_price as estimated_unit_price, ( select d.name from department d where d.id = f.department_id) as department, ( select e.full_name from employee e where e.id = f.created_by_id ) as employee, f.created_date as created_date , ( select extract(day from f.created_date)) as ageing_value from float f where f.retired = false";
  public static final String float_order_aging_analysis_query =
      "select f.float_order_ref as float_ref, upper(f.description) as item_description, f.amount as estimated_amount, ( select upper(d.name) from department d where d.id = f.department_id) as department, ( select upper(e.full_name) from employee e where e.id = f.created_by_id) as employee, upper(f.requested_by) as requested_by, upper(f.requested_by_phone_no) as requested_by_phone_no, f.created_date as created_date , (select current_date - f.created_date) as ageing_value, f.retired from float_order f";
  public static final String getFloat_order_aging_analysis_query_by_requester_email_count =
      "select count(fo.id) from float_order where fo.requested_by_email = :requested_by_email";
  public static final String getFloat_order_aging_analysis_query_by_requester_email =
      "select f.float_order_ref as float_ref, upper(f.description) as item_description, f.amount as estimated_amount, ( select upper(d.name) from department d where d.id = f.department_id) as department, ( select upper(e.full_name) from employee e where e.id = f.created_by_id) as employee, upper(f.requested_by) as requested_by, upper(f.requested_by_phone_no) as requested_by_phone_no, f.created_date as created_date , (select current_date - f.created_date) as ageing_value, f.retired from float_order f where f.requested_by_email = :requested_by_email order by f.id desc";
  static final String view_sql =
      "CREATE OR REPLACE VIEW public.request_per_current_month_per_department\n"
          + " AS\n"
          + " SELECT d.id,"
          + "    d.name AS department,"
          + "    count(r.id) AS num_of_request"
          + "   FROM ((department d"
          + "     JOIN employee e ON ((e.department_id = d.id)))"
          + "     JOIN request_item r ON ((r.employee_id = e.id)))"
          + "  WHERE (date_part('month'::text, r.created_date) = date_part('month'::text, CURRENT_DATE))"
          + "  GROUP BY d.name, d.id;";
  String cte_v =
      "with cte as ( select f.float_ref, f.item_description, f.quantity, f.estimated_unit_price, ( select d.name from department d where d.id = f.department_id) as department, ( select e.full_name from employee e where e.id = f.created_by_id ) as employee, f.created_date, ( select AGE(DATE(f.created_date))) as ageing_value from float f where f.retired = false) select *, max(cte.estimated_unit_price) over (partition by cte.department order by cte.ageing_value desc, cte.created_date ) highest_by_dept from cte";
  String db_function_for_request_items_without_quotations =
      "create or replace function get_request_item_ids_without_quotation() returns table (id int, name varchar) as $$ begin  return query \n"
          + "select ri.id, ri.name from request_item ri where ri.supplied_by is null and upper(ri.endorsement) = 'ENDORSED' and upper(ri.approval) = 'PENDING' and upper(status) = 'PENDING'\n"
          + "and ri.id in (select distinct(srm.request_item_id) from supplier_request_map srm) and ri.id not in (select riq.request_item_id from request_item_quotations riq);\n"
          + "end;\n"
          + "$$ language plpgsql;";

  public static final String ROLE_NOT_FOUND = "ROLE NOT FOUND";
  public static final String EMPLOYEE_NOT_FOUND = "EMPLOYEE NOT FOUND";
  public static final String FLOAT_NOT_FOUND = "FLOAT NOT FOUND";
  public static final String FILE_NOT_FOUND = "FILE NOT FOUND ";
  public static final String COMMENT_NOT_SAVED = "COMMENT NOT SAVED";
  public static final String COMMENT_SAVED = "COMMENT SAVED SUCCESSFULLY";
  public static final String FETCH_SUCCESSFUL = "FETCH SUCCESSFUL";
  public static final String UPDATE_SUCCESSFUL = "UPDATE SUCCESSFUL";
  public static final String REQUEST_CANCELLED = "REQUEST CANCELLED";

  public static final String FETCH_FLOAT_FAILED = "FETCH FLOAT FAILED";
  public static final String REPORT_GENERATION_FAILED = "REPORT GENERATION FAILED";
  public static final String NOT_FOUND = "NOT FOUND";

  public static final String CANCEL_PAYMENT_FAILED = "CANCEL PAYMENT FAILED";

  public static final String FLOAT_GRN_FAILED = "ISSUE FLOAT GRN FAILED";



}
