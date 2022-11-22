package com.logistics.supply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.supply.model.RequestItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import static org.springframework.util.StreamUtils.copyToString;

public final class TestUtil {


  private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  private static final ObjectMapper mapper = new ObjectMapper().setDateFormat(df);

  public static String getJsonFromResourcePath(String resourcePath) throws IOException {
    InputStream resourceAsStream =
        TestUtil.class.getClassLoader().getResourceAsStream(resourcePath);
    return copyToString(resourceAsStream, Charset.defaultCharset());
  }

  public static RequestItem requestItem(String path) throws IOException {
    String item = getJsonFromResourcePath(path);
    return mapper.readValue(item, RequestItem.class);
  }
}
