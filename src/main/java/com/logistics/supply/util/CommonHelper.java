package com.logistics.supply.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.logistics.supply.util.Constants.*;

@Slf4j
public class CommonHelper {

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
}
