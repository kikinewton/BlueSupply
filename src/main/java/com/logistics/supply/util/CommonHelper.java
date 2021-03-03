package com.logistics.supply.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CommonHelper {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static boolean isValidEmailAddress(String email){
        return email != null &&
                email.matches(EMAIL_REGEX);
    }

    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set emptyNames = new HashSet();
        for(java.beans.PropertyDescriptor pd : pds) {
            //check if value of this property is null then add it to the collection
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
            tmpNums = new String[]{phoneNumber};
        }
        log.info("Format these numbers: \n " + Arrays.toString(tmpNums));

        for (int i = 0; i < tmpNums.length; i++) {

            if (tmpNums[i].length() == 13) {
                formattedNo = tmpNums[i].substring(1).trim() + ",";
                result = result.append(formattedNo);
            } else if (tmpNums[i].length() == 12) {
                formattedNo = tmpNums[i].trim() + ",";
                result = result.append(formattedNo);
            } else if (tmpNums[i].length() == 10) {
                formattedNo = String.format("233%s", tmpNums[i].substring(1)).trim() + ",";
                result = result.append(formattedNo);
            } else if (tmpNums[i].length() == 9) {
                formattedNo = "233" + tmpNums[i].trim() + ",";
                result = result.append(formattedNo);
            } else if (tmpNums[i].length() == 11) {
                formattedNo = "233" + tmpNums[i].substring(1).replace(" ", "").trim() + ",";
                result = result.append(formattedNo);
            } else if (tmpNums[i].contains(",")) {
                formattedNo = tmpNums[i];
            }

        }

        return result.deleteCharAt(result.length() - 1).toString();
    }
}
