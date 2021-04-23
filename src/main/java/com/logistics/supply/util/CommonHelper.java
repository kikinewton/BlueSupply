package com.logistics.supply.util;

import com.logistics.supply.enums.EmployeeLevel;
import com.logistics.supply.model.Department;
import com.logistics.supply.model.Employee;
import com.logistics.supply.repository.EmployeeRepository;
import com.logistics.supply.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static com.logistics.supply.util.Constants.*;

@Slf4j
public class CommonHelper {

  @Autowired private EmployeeRepository employeeRepository;

  private static final Random alphaIdRandom = new Random();
  private static final String EMAIL_REGEX =
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

  private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public static boolean isValidEmailAddress(String email) {
    return email != null && email.matches(EMAIL_REGEX);
  }

  public static boolean MatchBCryptPassword(String hashedPassword, String rawPassword) {
    return encoder.matches(rawPassword, hashedPassword);
  }

  public static String GenerateBCryptEncoder(String rawPassword) {
    return encoder.encode(rawPassword);
  }

  public static String generatePassword(String p_str, int p_target_length) {
    int l_length;
    StringBuffer l_buf = null;
    byte l_byte[] = new byte[1];

    if (p_str == null) {
      l_buf = new StringBuffer();
      l_length = 0;
    } else if ((l_length = p_str.length()) == 0) {
      l_buf = new StringBuffer();
      l_length = 0;
    } else if (l_length < p_target_length) {
      l_buf = new StringBuffer(p_str);
    } else {
      return p_str;
    }

    for (; l_length < p_target_length; l_length++) {
      alphaIdRandom.nextBytes(l_byte);
      l_buf.append((char) ((Math.abs(l_byte[0]) % 26) + 'A'));
    }

    return l_buf.toString();
  }

  public static String[] getNullPropertyNames(Object source) {
    final BeanWrapper src = new BeanWrapperImpl(source);
    java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
    Set emptyNames = new HashSet();
    for (java.beans.PropertyDescriptor pd : pds) {
      // check if value of this property is null then add it to the collection
      Object srcValue = src.getPropertyValue(pd.getName());
      if (srcValue == null) emptyNames.add(pd.getName());
    }
    String[] result = new String[emptyNames.size()];
    return (String[]) emptyNames.toArray(result);
  }

  public static Date calculateRenewalDate(int months) {
    if (Objects.isNull(months)) months = 1;
    Calendar cal = Calendar.getInstance();
    switch (months) {
      case 1:
        cal.add(Calendar.DATE, 30);
        break;
      case 3:
        cal.add(Calendar.DATE, 91);
        break;
      case 6:
        cal.add(Calendar.DATE, 182);
        break;
      case 12:
        cal.add(Calendar.DATE, 365);
        break;
      default:
        cal.add(Calendar.DATE, 30);
        break;
    }
    return cal.getTime();
  }

  public static Date calculatePaymentDate(int days) {
    if (Objects.isNull(days)) days = 0;
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, days);
    return calendar.getTime();
  }

  private String getFormattedPhoneNumber(String phoneNumber) {
    String[] tmpNums = null;
    String formattedNo = null;
    StringBuilder result = new StringBuilder();

    if (phoneNumber.replace(" ", "").contains(",")) {
      tmpNums = phoneNumber.split(",");
    } else {
      tmpNums = new String[] {phoneNumber};
    }
    log.info("Format these numbers: \n " + Arrays.toString(tmpNums));

    for (String tmpNum : tmpNums) {

      if (tmpNum.length() == 13) {
        formattedNo = tmpNum.substring(1).trim() + ",";
        result = result.append(formattedNo);
      } else if (tmpNum.length() == 12) {
        formattedNo = tmpNum.trim() + ",";
        result = result.append(formattedNo);
      } else if (tmpNum.length() == 10) {
        formattedNo = String.format("233%s", tmpNum.substring(1)).trim() + ",";
        result = result.append(formattedNo);
      } else if (tmpNum.length() == 9) {
        formattedNo = "233" + tmpNum.trim() + ",";
        result = result.append(formattedNo);
      } else if (tmpNum.length() == 11) {
        formattedNo = "233" + tmpNum.substring(1).replace(" ", "").trim() + ",";
        result = result.append(formattedNo);
      } else if (tmpNum.contains(",")) {
        formattedNo = tmpNum;
      }
    }

    return result.deleteCharAt(result.length() - 1).toString();
  }

  public static String buildEmail(String name, String link, String title, String message) {
    String from;
    switch (title) {
      case PROCUREMENT_DETAILS_MAIL:
        from = "HOD";
        break;
      case REQUEST_APPROVAL_MAIL:
        from = "Procurement Team";
        break;
      default:
        from = "";
        break;
    }
    title = title.replace("_", " ");

    return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n"
        + "\n"
        + "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n"
        + "\n"
        + "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
        + "    <tbody><tr>\n"
        + "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n"
        + "        \n"
        + "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n"
        + "          <tbody><tr>\n"
        + "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n"
        + "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
        + "                  <tbody><tr>\n"
        + "                    <td style=\"padding-left:10px\">\n"
        + "                  \n"
        + "                    </td>\n"
        + "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n"
        + "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\"> "
        + title
        + " </span>\n"
        + "                    </td>\n"
        + "                  </tr>\n"
        + "                </tbody></table>\n"
        + "              </a>\n"
        + "            </td>\n"
        + "          </tr>\n"
        + "        </tbody></table>\n"
        + "        \n"
        + "      </td>\n"
        + "    </tr>\n"
        + "  </tbody></table>\n"
        + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
        + "    <tbody><tr>\n"
        + "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n"
        + "      <td>\n"
        + "        \n"
        + "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
        + "                  <tbody><tr>\n"
        + "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n"
        + "                  </tr>\n"
        + "                </tbody></table>\n"
        + "        \n"
        + "      </td>\n"
        + "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n"
        + "    </tr>\n"
        + "  </tbody></table>\n"
        + "\n"
        + "\n"
        + "\n"
        + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
        + "    <tbody><tr>\n"
        + "      <td height=\"30\"><br></td>\n"
        + "    </tr>\n"
        + "    <tr>\n"
        + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
        + "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n"
        + "        \n"
        + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Dear "
        + name
        + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">"
        + message
        + " </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\""
        + link
        + "\"> "
        + title
        + " </a> </p></blockquote>\n "
        + from
        + " <p>Thank you</p>"
        + "        \n"
        + "      </td>\n"
        + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
        + "    </tr>\n"
        + "    <tr>\n"
        + "      <td height=\"30\"><br></td>\n"
        + "    </tr>\n"
        + "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n"
        + "\n"
        + "</div></div>";
  }

  public static String buildNewUserEmail(
      String name, String link, String title, String message, String credentials) {
    String from;
    switch (title) {
      case PROCUREMENT_DETAILS_MAIL:
        from = "HOD";
        break;
      case REQUEST_APPROVAL_MAIL:
        from = "Procurement Team";
        break;
      default:
        from = "";
        break;
    }
    title = title.replace("_", " ");

    return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n"
        + "\n"
        + "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n"
        + "\n"
        + "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
        + "    <tbody><tr>\n"
        + "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n"
        + "        \n"
        + "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n"
        + "          <tbody><tr>\n"
        + "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n"
        + "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
        + "                  <tbody><tr>\n"
        + "                    <td style=\"padding-left:10px\">\n"
        + "                  \n"
        + "                    </td>\n"
        + "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n"
        + "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\"> "
        + title
        + " </span>\n"
        + "                    </td>\n"
        + "                  </tr>\n"
        + "                </tbody></table>\n"
        + "              </a>\n"
        + "            </td>\n"
        + "          </tr>\n"
        + "        </tbody></table>\n"
        + "        \n"
        + "      </td>\n"
        + "    </tr>\n"
        + "  </tbody></table>\n"
        + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
        + "    <tbody><tr>\n"
        + "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n"
        + "      <td>\n"
        + "        \n"
        + "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n"
        + "                  <tbody><tr>\n"
        + "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n"
        + "                  </tr>\n"
        + "                </tbody></table>\n"
        + "        \n"
        + "      </td>\n"
        + "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n"
        + "    </tr>\n"
        + "  </tbody></table>\n"
        + "\n"
        + "\n"
        + "\n"
        + "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n"
        + "    <tbody><tr>\n"
        + "      <td height=\"30\"><br></td>\n"
        + "    </tr>\n"
        + "    <tr>\n"
        + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
        + "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n"
        + "        \n"
        + "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Dear "
        + name
        + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">"
        + message
        + " </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> "
        + credentials
        + " </p></blockquote>\n "
        + from
        + " <p>Thank you</p>"
        + "        \n"
        + "      </td>\n"
        + "      <td width=\"10\" valign=\"middle\"><br></td>\n"
        + "    </tr>\n"
        + "    <tr>\n"
        + "      <td height=\"30\"><br></td>\n"
        + "    </tr>\n"
        + "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n"
        + "\n"
        + "</div></div>";
  }

  public static String buildNewHtmlEmail(String link, String name, String message) {

    return "<html>\n"
        + "  <head>\n"
        + "    <style id=\"mldStyle\" type=\"text/css\">\n"
        + "      body {\n"
        + "        width: 100%;\n"
        + "        height: 100%;\n"
        + "        mso-line-height-rule: exactly;\n"
        + "      }\n"
        + "      a {\n"
        + "        text-decoration: none;\n"
        + "      }\n"
        + "      .mld-force-underline a {\n"
        + "        text-decoration: underline;\n"
        + "      }\n"
        + "      * {\n"
        + "        word-break: normal;\n"
        + "      }\n"
        + "      table {\n"
        + "        border-collapse: collapse;\n"
        + "      }\n"
        + "      .mld-desktop-hide {\n"
        + "        display: none !important;\n"
        + "        mso-hide: all;\n"
        + "      }\n"
        + "      ul li {\n"
        + "        mso-special-format: bullet;\n"
        + "        mso-line-height-alt: 1em;\n"
        + "      }\n"
        + "      ol,\n"
        + "      ol li {\n"
        + "        list-style-type: decimal !important;\n"
        + "      }\n"
        + "      ol li {\n"
        + "        mso-text-indent-alt: 20px;\n"
        + "        mso-line-height-alt: 1em;\n"
        + "      }\n"
        + "      @media screen and (max-device-width: 600px),\n"
        + "        screen and (max-width: 600px) {\n"
        + "        .mld-mobile-hide {\n"
        + "          display: none !important;\n"
        + "        }\n"
        + "        .mld-desktop-hide {\n"
        + "          display: table !important;\n"
        + "          mso-hide: none;\n"
        + "        }\n"
        + "        .mld-full-width-in-mobile {\n"
        + "          width: auto !important;\n"
        + "          height: auto !important;\n"
        + "          max-width: 100%;\n"
        + "        }\n"
        + "      }\n"
        + "      #workspace .mld-desktop-hide {\n"
        + "        display: table !important;\n"
        + "      }\n"
        + "    </style>\n"
        + "    <style type=\"text/css\">\n"
        + "      /* Outlook link fix */.number {color:#b1b4c0 !important; text-decoration: none !important;}#outlook a {padding:0;}.ExternalClass {width:100% !important;}.ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font,.ExternalClass td, .ExternalClass div {line-height: 100%;}img {outline:none; text-decoration:none; -ms-interpolation-mode: bicubic;}a img {border:none;}.ExternalClass img[class^=Emoji] { width: 10px !important; height: 10px !important; display: inline !important; }@media screen and (max-device-width: 600px), screen and (max-width: 600px) {.mobile100{width:100% !important; height:auto !important;}.mobilesplit{width:100%!important; float:left!important;}.padfix{padding:0px 10px !important; float:left!important;}.nosee{display:none !important;}}@media screen and (max-device-width: 500px), screen and (max-width: 500px) {.mobile1001{width:100% !important; height:auto !important;}}@media screen and (max-device-width: 400px), screen and (max-width: 400px) {.mobile1002{width:100% !important; height:auto !important;}}.mld-def-paragraph{font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:normal;font-style:normal;text-decoration:none;text-align:left;color:#000000;line-height:21px;margin:0px;padding:0;}.mld-def-heading1{font-family:Arial,Helvetica,sans-serif;font-size:24px;font-weight:bold;font-style:normal;text-decoration:none;text-align:left;color:#000000;line-height:36px;margin:0px;padding:0;}.mld-def-heading2{font-family:Arial,Helvetica,sans-serif;font-size:20px;font-weight:normal;font-style:normal;text-decoration:none;text-align:left;color:#000000;line-height:30px;margin:0px;padding:0;}.mld-def-custom{font-family:Arial,Helvetica,sans-serif;font-size:12px;font-weight:normal;font-style:normal;text-decoration:none;text-align:left;color:#000000;line-height:18px;margin:0px;padding:0;}.mld-def-link{font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:normal;font-style:normal;text-decoration:underline;text-align:left;color:#000000;line-height:21px;margin:0px;padding:0;}.mld-def-link-heading1{font-family:Arial,Helvetica,sans-serif;font-size:24px;font-weight:bold;font-style:normal;text-decoration:underline;text-align:left;color:#000000;line-height:36px;margin:0px;padding:0;}.mld-def-link-heading2{font-family:Arial,Helvetica,sans-serif;font-size:20px;font-weight:normal;font-style:normal;text-decoration:underline;text-align:left;color:#000000;line-height:30px;margin:0px;padding:0;}.mld-def-link-custom{font-family:Arial,Helvetica,sans-serif;font-size:14px;font-weight:normal;font-style:normal;text-decoration:underline;text-align:left;color:#000000;line-height:21px;margin:0px;padding:0;}.mld-def-block-block{padding:15px 20px 15px 20px;}.mld-def-block-block-outer{padding:0px;}.mld-def-block-title{padding:15px 20px 5px 20px;align:center;}.mld-def-block-title-outer{padding:0px;}.mld-def-block-text{padding:5px 20px 15px 20px;}.mld-def-block-text-outer{padding:0px;}.mld-def-block-image{padding:15px 20px 15px 20px;}.mld-def-block-image-outer{padding:0px;}.mld-def-block-image-img{width:auto;align:center;}.mld-def-block-button{padding:5px 0px 5px 0px;width:100px;background-color:#606060;border-radius:5px;overflow:hidden;display:block;color:#FFFFFF;font-family:arial;font-size:13px;font-weight:bold;line-height:19px;text-decoration:none;}.mld-def-block-button-outer{padding:5px 20px 15px 20px;}.mld-def-block-button-icon{}.mld-def-block-button-text{color:#FFFFFF;font-family:Arial;font-size:15px;line-height:22px;text-decoration:none;text-align:center;}.mld-def-block-divider{padding:15px 20px 15px 20px;border-top:1px solid #E5E5E5;}.mld-def-block-divider-outer{padding:0px;}.mld-def-block-container{}.mld-def-block-container-outer{padding:0px;}.mld-def-block-share{padding:15px 20px 15px 20px;align:left;}.mld-def-block-share-outer{padding:0px;}.mld-def-block-follow{padding:15px 20px 15px 20px;align:left;}.mld-def-block-follow-outer{padding:0px;}.mld-def-block-rss{padding:15px 20px 15px 20px;}.mld-def-block-rss-outer{padding:0px;}.mld-def-block-dynamic{padding:15px 20px 15px 20px;}.mld-def-block-dynamic-outer{padding:0px;}.mld-def-block-video{padding:15px 20px 15px 20px;}.mld-def-block-video-outer{padding:0px;}-->\n"
        + "    </style>\n"
        + "  </head>\n"
        + "  <body style=\"overflow: hidden\">\n"
        + "    <table\n"
        + "      border=\"0\"\n"
        + "      cellspacing=\"0\"\n"
        + "      cellpadding=\"0\"\n"
        + "      style=\"width: 100%; background-color: #eeeeee\"\n"
        + "    >\n"
        + "      <tbody>\n"
        + "        <tr class=\"mld-header\">\n"
        + "          <td align=\"center\" valign=\"top\">\n"
        + "            <table\n"
        + "              border=\"0\"\n"
        + "              cellspacing=\"0\"\n"
        + "              cellpadding=\"0\"\n"
        + "              class=\"mobile100\"\n"
        + "              style=\"width: 600px\"\n"
        + "            >\n"
        + "              <tbody>\n"
        + "                <tr>\n"
        + "                  <td\n"
        + "                    align=\"center\"\n"
        + "                    valign=\"top\"\n"
        + "                    class=\"mld-part\"\n"
        + "                    style=\"width: 100%\"\n"
        + "                  >\n"
        + "                    <table\n"
        + "                      border=\"0\"\n"
        + "                      cellspacing=\"0\"\n"
        + "                      cellpadding=\"0\"\n"
        + "                      class=\"mld-element mld-container mldElementCaller\"\n"
        + "                      style=\"width: 100% !important\"\n"
        + "                    >\n"
        + "                      <tbody>\n"
        + "                        <tr>\n"
        + "                          <td\n"
        + "                            align=\"left\"\n"
        + "                            valign=\"top\"\n"
        + "                            style=\"padding: 0px 0px 0px 0px\"\n"
        + "                          >\n"
        + "                            <table\n"
        + "                              border=\"0\"\n"
        + "                              cellspacing=\"0\"\n"
        + "                              cellpadding=\"0\"\n"
        + "                              style=\"width: 100%\"\n"
        + "                            >\n"
        + "                              <tbody>\n"
        + "                                <tr>\n"
        + "                                  <td\n"
        + "                                    align=\"left\"\n"
        + "                                    valign=\"top\"\n"
        + "                                    style=\"width: 50%\"\n"
        + "                                    class=\"mld-element-content mobilesplit\"\n"
        + "                                  >\n"
        + "                                    <table\n"
        + "                                      border=\"0\"\n"
        + "                                      cellspacing=\"0\"\n"
        + "                                      cellpadding=\"0\"\n"
        + "                                      style=\"width: 100% !important\"\n"
        + "                                      class=\"mld-element mld-block mld-block-text mldElementCaller\"\n"
        + "                                    >\n"
        + "                                      <tbody>\n"
        + "                                        <tr>\n"
        + "                                          <td align=\"left\" valign=\"top\">\n"
        + "                                            <table\n"
        + "                                              border=\"0\"\n"
        + "                                              cellspacing=\"0\"\n"
        + "                                              cellpadding=\"0\"\n"
        + "                                              style=\"width: 100%\"\n"
        + "                                            >\n"
        + "                                              <tbody>\n"
        + "                                                <tr>\n"
        + "                                                  <td\n"
        + "                                                    align=\"left\"\n"
        + "                                                    valign=\"middle\"\n"
        + "                                                    class=\"mld-element-content\"\n"
        + "                                                    style=\"\n"
        + "                                                      padding: 5px 20px 5px 20px;\n"
        + "                                                    \"\n"
        + "                                                  ></td>\n"
        + "                                                </tr>\n"
        + "                                              </tbody>\n"
        + "                                            </table>\n"
        + "                                          </td>\n"
        + "                                        </tr>\n"
        + "                                      </tbody>\n"
        + "                                    </table>\n"
        + "                                  </td>\n"
        + "                                  <td\n"
        + "                                    align=\"left\"\n"
        + "                                    valign=\"top\"\n"
        + "                                    style=\"width: 50%\"\n"
        + "                                    class=\"mld-element-content mobilesplit\"\n"
        + "                                  >\n"
        + "                                    <table\n"
        + "                                      border=\"0\"\n"
        + "                                      cellspacing=\"0\"\n"
        + "                                      cellpadding=\"0\"\n"
        + "                                      style=\"width: 100% !important\"\n"
        + "                                      class=\"mld-element mld-block mld-block-text mld-mobile-hide mldElementCaller\"\n"
        + "                                    >\n"
        + "                                      <tbody>\n"
        + "                                        <tr>\n"
        + "                                          <td align=\"right\" valign=\"top\">\n"
        + "                                            <table\n"
        + "                                              border=\"0\"\n"
        + "                                              cellspacing=\"0\"\n"
        + "                                              cellpadding=\"0\"\n"
        + "                                              style=\"width: 100%\"\n"
        + "                                            >\n"
        + "                                              <tbody>\n"
        + "                                                <tr>\n"
        + "                                                  <td\n"
        + "                                                    align=\"right\"\n"
        + "                                                    valign=\"middle\"\n"
        + "                                                    class=\"mld-element-content\"\n"
        + "                                                    style=\"\n"
        + "                                                      padding: 5px 20px 5px 20px;\n"
        + "                                                    \"\n"
        + "                                                  ></td>\n"
        + "                                                </tr>\n"
        + "                                              </tbody>\n"
        + "                                            </table>\n"
        + "                                          </td>\n"
        + "                                        </tr>\n"
        + "                                      </tbody>\n"
        + "                                    </table>\n"
        + "                                  </td>\n"
        + "                                </tr>\n"
        + "                              </tbody>\n"
        + "                            </table>\n"
        + "                          </td>\n"
        + "                        </tr>\n"
        + "                      </tbody>\n"
        + "                    </table>\n"
        + "                  </td>\n"
        + "                </tr>\n"
        + "              </tbody>\n"
        + "            </table>\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "        <tr class=\"mld-body mld-body-1\">\n"
        + "          <td align=\"center\" valign=\"top\">\n"
        + "            <table\n"
        + "              border=\"0\"\n"
        + "              cellspacing=\"0\"\n"
        + "              cellpadding=\"0\"\n"
        + "              class=\"mobile100\"\n"
        + "              style=\"width: 600px\"\n"
        + "            >\n"
        + "              <tbody>\n"
        + "                <tr>\n"
        + "                  <td\n"
        + "                    align=\"center\"\n"
        + "                    valign=\"top\"\n"
        + "                    class=\"mld-part\"\n"
        + "                    style=\"background-color: #ffffff\"\n"
        + "                  >\n"
        + "                    <table\n"
        + "                      border=\"0\"\n"
        + "                      cellspacing=\"0\"\n"
        + "                      cellpadding=\"0\"\n"
        + "                      class=\"mld-element mld-container mldElementCaller\"\n"
        + "                      style=\"width: 100% !important\"\n"
        + "                    >\n"
        + "                      <tbody>\n"
        + "                        <tr>\n"
        + "                          <td\n"
        + "                            align=\"left\"\n"
        + "                            valign=\"top\"\n"
        + "                            style=\"padding: 0px 0px 0px 0px\"\n"
        + "                            class=\"\"\n"
        + "                          >\n"
        + "                            <table\n"
        + "                              border=\"0\"\n"
        + "                              cellspacing=\"0\"\n"
        + "                              cellpadding=\"0\"\n"
        + "                              style=\"width: 100%\"\n"
        + "                              class=\"\"\n"
        + "                            >\n"
        + "                              <tbody>\n"
        + "                                <tr>\n"
        + "                                  <td\n"
        + "                                    align=\"left\"\n"
        + "                                    valign=\"top\"\n"
        + "                                    class=\"mld-element-content mobilesplit\"\n"
        + "                                    style=\"width: 600px; padding: 0px\"\n"
        + "                                  >\n"
        + "                                    <table\n"
        + "                                      border=\"0\"\n"
        + "                                      cellspacing=\"0\"\n"
        + "                                      cellpadding=\"0\"\n"
        + "                                      style=\"width: 100% !important\"\n"
        + "                                      class=\"mld-element mld-block mld-block-text mldElementCaller\"\n"
        + "                                    >\n"
        + "                                      <tbody>\n"
        + "                                        <tr>\n"
        + "                                          <td\n"
        + "                                            align=\"left\"\n"
        + "                                            valign=\"top\"\n"
        + "                                            class=\"\"\n"
        + "                                          >\n"
        + "                                            <table\n"
        + "                                              border=\"0\"\n"
        + "                                              cellspacing=\"0\"\n"
        + "                                              cellpadding=\"0\"\n"
        + "                                              style=\"\n"
        + "                                                width: 100%;\n"
        + "                                                background-color: #45b3e0;\n"
        + "                                              \"\n"
        + "                                              class=\"\"\n"
        + "                                            >\n"
        + "                                              <tbody>\n"
        + "                                                <tr>\n"
        + "                                                  <td\n"
        + "                                                    align=\"center\"\n"
        + "                                                    valign=\"middle\"\n"
        + "                                                    class=\"mld-element-content\"\n"
        + "                                                    style=\"padding: 15px 20px\"\n"
        + "                                                  >\n"
        + "                                                    <p\n"
        + "                                                      style=\"\n"
        + "                                                        color: #ffffff;\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        font-size: 30px;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    >\n"
        + "                                                      <strong\n"
        + "                                                        >Blue Skies Ltd<br /><em\n"
        + "                                                          ><span\n"
        + "                                                            style=\"\n"
        + "                                                              font-size: 18px;\n"
        + "                                                              font-family: Georgia,\n"
        + "                                                                serif;\n"
        + "                                                            \"\n"
        + "                                                            >For the Love of\n"
        + "                                                            Fresh</span\n"
        + "                                                          ></em\n"
        + "                                                        ></strong\n"
        + "                                                      >\n"
        + "                                                    </p>\n"
        + "                                                  </td>\n"
        + "                                                </tr>\n"
        + "                                              </tbody>\n"
        + "                                            </table>\n"
        + "                                          </td>\n"
        + "                                        </tr>\n"
        + "                                      </tbody>\n"
        + "                                    </table>\n"
        + "                                    <table\n"
        + "                                      border=\"0\"\n"
        + "                                      cellspacing=\"0\"\n"
        + "                                      cellpadding=\"0\"\n"
        + "                                      style=\"width: 100% !important\"\n"
        + "                                      class=\"mld-element mld-block mld-block-text mldElementCaller\"\n"
        + "                                    >\n"
        + "                                      <tbody>\n"
        + "                                        <tr>\n"
        + "                                          <td\n"
        + "                                            align=\"left\"\n"
        + "                                            valign=\"top\"\n"
        + "                                            class=\"\"\n"
        + "                                          >\n"
        + "                                            <table\n"
        + "                                              border=\"0\"\n"
        + "                                              cellspacing=\"0\"\n"
        + "                                              cellpadding=\"0\"\n"
        + "                                              style=\"width: 100%\"\n"
        + "                                              class=\"\"\n"
        + "                                            >\n"
        + "                                              <tbody>\n"
        + "                                                <tr>\n"
        + "                                                  <td\n"
        + "                                                    align=\"left\"\n"
        + "                                                    valign=\"top\"\n"
        + "                                                    class=\"mld-element-content\"\n"
        + "                                                    style=\"\n"
        + "                                                      padding: 15px 20px;\n"
        + "                                                      width: 560px;\n"
        + "                                                    \"\n"
        + "                                                  >\n"
        + "                                                    <p\n"
        + "                                                      class=\"mld-heading1\"\n"
        + "                                                      style=\"\n"
        + "                                                        color: #000000;\n"
        + "                                                        font-size: 24px;\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        padding: 0px;\n"
        + "                                                        text-align: center;\n"
        + "                                                      \"\n"
        + "                                                    >\n"
        + "                                                      <br />\n"
        + "                                                    </p>\n"
        + "                                                    <p\n"
        + "                                                      class=\"mld-paragraph\"\n"
        + "                                                      style=\"\n"
        + "                                                        color: #000000;\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        font-size: 14px;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    >\n"
        + "                                                      <span\n"
        + "                                                        style=\"\n"
        + "                                                          font-family: 'Palatino Linotype',\n"
        + "                                                            'Book Antiqua',\n"
        + "                                                            Palatino, serif;\n"
        + "                                                        \"\n"
        + "                                                        >Dear "
        + name
        + "</span\n"
        + "                                                      >\n"
        + "                                                    </p>\n"
        + "                                                    <p\n"
        + "                                                      class=\"mld-paragraph\"\n"
        + "                                                      style=\"\n"
        + "                                                        color: #000000;\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        font-size: 14px;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    >\n"
        + "                                                      <span\n"
        + "                                                        style=\"\n"
        + "                                                          font-family: 'Palatino Linotype',\n"
        + "                                                            'Book Antiqua',\n"
        + "                                                            Palatino, serif;\n"
        + "                                                        \"\n"
        + "                                                      >\n"
        + message
        + "                                                      \n"
        + "                                                    </p>\n"
        + "                                                    <p\n"
        + "                                                      style=\"\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        font-size: 14px;\n"
        + "                                                        color: #000000;\n"
        + "                                                        line-height: 21px;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    >\n"
        + "                                                      <br />\n"
        + "                                                    </p>\n"
        + "                                                    <blockquote \n"
        + "                                                    style=\"\n"
        + "                                                    Margin:0 0 20px 0;\n"
        + "                                                    border-left:10px solid #b1b4b6;\n"
        + "                                                    padding:15px 0 0.1px 15px;\n"
        + "                                                    font-size:19px;\n"
        + "                                                    line-height:25px\n"
        + "                                                    \n"
        + "                                                    <p \n"
        + "                                                    style=Margin:0 0 20px 0;\n"
        + "                                                    font-size:19px;\n"
        + "                                                    line-height:25px;\n"
        + "                                                    color: #0b0c0c\\> \n"
        + "                                                    <a href=\\\"\n"
        + "         link\n"
        + "        \\> \n"
        + "         "
        + link
        + "\n"
        + "        </a> </p></blockquote>\n"
        + "                                                    <p\n"
        + "                                                      class=\"mld-paragraph\"\n"
        + "                                                      style=\"\n"
        + "                                                        color: #000000;\n"
        + "                                                        font-family: Arial, Helvetica, sans-serif;\n"
        + "                                                        font-size: 14px;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    >Thank you\n"
        + "                                                      <br />\n"
        + "                                                    </p>\n"
        + "                                                    <!--[if mso]></td>\n"
        + "</tr>\n"
        + "</tbody>\n"
        + "</table>\n"
        + "<![endif]-->\n"
        + "                                                  </td>\n"
        + "                                                </tr>\n"
        + "                                              </tbody>\n"
        + "                                            </table>\n"
        + "                                          </td>\n"
        + "                                        </tr>\n"
        + "                                      </tbody>\n"
        + "                                    </table>\n"
        + "                                  </td>\n"
        + "                                </tr>\n"
        + "                              </tbody>\n"
        + "                            </table>\n"
        + "                          </td>\n"
        + "                        </tr>\n"
        + "                      </tbody>\n"
        + "                    </table>\n"
        + "                  </td>\n"
        + "                </tr>\n"
        + "              </tbody>\n"
        + "            </table>\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "        <tr class=\"mld-footer\">\n"
        + "          <td align=\"center\" valign=\"top\">\n"
        + "            <table\n"
        + "              border=\"0\"\n"
        + "              cellspacing=\"0\"\n"
        + "              cellpadding=\"0\"\n"
        + "              class=\"mobile100\"\n"
        + "              style=\"width: 600px\"\n"
        + "            >\n"
        + "              <tbody>\n"
        + "                <tr>\n"
        + "                  <td\n"
        + "                    align=\"center\"\n"
        + "                    valign=\"top\"\n"
        + "                    class=\"mld-part\"\n"
        + "                    style=\"width: 100%\"\n"
        + "                  >\n"
        + "                    <table\n"
        + "                      border=\"0\"\n"
        + "                      cellspacing=\"0\"\n"
        + "                      cellpadding=\"0\"\n"
        + "                      class=\"mld-element mld-container mldElementCaller\"\n"
        + "                      style=\"width: 100% !important\"\n"
        + "                    >\n"
        + "                      <tbody>\n"
        + "                        <tr>\n"
        + "                          <td\n"
        + "                            align=\"left\"\n"
        + "                            valign=\"top\"\n"
        + "                            style=\"padding: 0px 0px 0px 0px\"\n"
        + "                          >\n"
        + "                            <table\n"
        + "                              border=\"0\"\n"
        + "                              cellspacing=\"0\"\n"
        + "                              cellpadding=\"0\"\n"
        + "                              style=\"width: 100%\"\n"
        + "                            >\n"
        + "                              <tbody>\n"
        + "                                <tr>\n"
        + "                                  <td\n"
        + "                                    align=\"left\"\n"
        + "                                    valign=\"top\"\n"
        + "                                    class=\"mld-element-content mobilesplit\"\n"
        + "                                    style=\"width: 600px\"\n"
        + "                                  >\n"
        + "                                    <table\n"
        + "                                      border=\"0\"\n"
        + "                                      cellspacing=\"0\"\n"
        + "                                      cellpadding=\"0\"\n"
        + "                                      style=\"width: 100% !important\"\n"
        + "                                      class=\"mld-element mld-block mld-block-text mldElementCaller\"\n"
        + "                                    >\n"
        + "                                      <tbody>\n"
        + "                                        <tr>\n"
        + "                                          <td align=\"left\" valign=\"top\">\n"
        + "                                            <table\n"
        + "                                              border=\"0\"\n"
        + "                                              cellspacing=\"0\"\n"
        + "                                              cellpadding=\"0\"\n"
        + "                                              style=\"\n"
        + "                                                width: 100%;\n"
        + "                                                background-color: #eeeeee;\n"
        + "                                              \"\n"
        + "                                            >\n"
        + "                                              <tbody>\n"
        + "                                                <tr>\n"
        + "                                                  <td\n"
        + "                                                    align=\"left\"\n"
        + "                                                    valign=\"top\"\n"
        + "                                                    class=\"mld-element-content\"\n"
        + "                                                    style=\"\n"
        + "                                                      padding: 15px 20px 15px\n"
        + "                                                        20px;\n"
        + "                                                    \"\n"
        + "                                                  >\n"
        + "                                                    <!--[if mso]><table style=\"width:560px;\">\n"
        + "<tbody>\n"
        + "<tr>\n"
        + "<td style=\"width:560px;\" align=\"left\" valign=\"top\">\n"
        + "<![endif]-->\n"
        + "                                                    <p\n"
        + "                                                      style=\"\n"
        + "                                                        font-size: 11px;\n"
        + "                                                        font-family: Arial,\n"
        + "                                                          Helvetica, sans-serif;\n"
        + "                                                        color: #666666;\n"
        + "                                                        margin: 0px;\n"
        + "                                                        line-height: 1.5;\n"
        + "                                                        padding: 0px;\n"
        + "                                                      \"\n"
        + "                                                    ></p>\n"
        + "                                                  </td>\n"
        + "                                                </tr>\n"
        + "                                              </tbody>\n"
        + "                                            </table>\n"
        + "                                          </td>\n"
        + "                                        </tr>\n"
        + "                                      </tbody>\n"
        + "                                    </table>\n"
        + "                                  </td>\n"
        + "                                </tr>\n"
        + "                              </tbody>\n"
        + "                            </table>\n"
        + "                          </td>\n"
        + "                        </tr>\n"
        + "                      </tbody>\n"
        + "                    </table>\n"
        + "                  </td>\n"
        + "                </tr>\n"
        + "              </tbody>\n"
        + "            </table>\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "      </tbody>\n"
        + "    </table>\n"
        + "  </body>\n"
        + "</html>\n";
  }
}
