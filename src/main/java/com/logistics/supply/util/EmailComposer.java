package com.logistics.supply.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailComposer {

  public String buildEmailWithTable(String title,String message, String table) {
    return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
        + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\">\n"
        + "  <head>\n"
        + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
        + "    <!-- NAME: 1 COLUMN -->\n"
        + "    <!--[if gte mso 15]>\n"
        + "      <xml>\n"
        + "        <o:OfficeDocumentSettings>\n"
        + "          <o:AllowPNG />\n"
        + "          <o:PixelsPerInch>96</o:PixelsPerInch>\n"
        + "        </o:OfficeDocumentSettings>\n"
        + "      </xml>\n"
        + "    <![endif]-->\n"
        + "    <meta charset=\"UTF-8\" />\n"
        + "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n"
        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
        + "    <title>"+title+"</title>\n"
        + "\n"
        + "    <style>\n"
        + "      img {\n"
        + "        border: 0;\n"
        + "        height: auto;\n"
        + "        outline: none;\n"
        + "        text-decoration: none;\n"
        + "      }\n"
        + "\n"
        + "      body {\n"
        + "        height: 100%;\n"
        + "        margin: 0;\n"
        + "        padding: 0;\n"
        + "        width: 100%;\n"
        + "      }\n"
        + "\n"
        + "      .styled-table {\n"
        + "        border-collapse: collapse;\n"
        + "        margin: 25px 0;\n"
        + "        font-size: 0.95em;\n"
        + "        font-family: HelveticaNeue-Light;\n"
        + "        min-width: 400px;\n"
        + "        box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);\n"
        + "      }\n"
        + "\n"
        + "      .styled-table thead tr {\n"
        + "        background-color: #009879;\n"
        + "        color: #ffffff;\n"
        + "        text-align: left;\n"
        + "      }\n"
        + "\n"
        + "      .styled-table tbody tr {\n"
        + "        border-bottom: 1px solid #dddddd;\n"
        + "      }\n"
        + "\n"
        + "      .styled-table tbody tr:nth-of-type(even) {\n"
        + "        background-color: #f3f3f3;\n"
        + "      }\n"
        + "\n"
        + "      .styled-table tbody tr:last-of-type {\n"
        + "        border-bottom: 2px solid #0075c9;\n"
        + "      }\n"
        + "\n"
        + "      .styled-table th,\n"
        + "      .styled-table td {\n"
        + "        padding: 12px 15px;\n"
        + "      }\n"
        + "\n"
        + "      img {\n"
        + "        -ms-interpolation-mode: bicubic;\n"
        + "      }\n"
        + "\n"
        + "      body {\n"
        + "        -ms-text-size-adjust: 100%;\n"
        + "        -webkit-text-size-adjust: 100%;\n"
        + "      }\n"
        + "\n"
        + "      body {\n"
        + "        background-color: #fafafa;\n"
        + "      }\n"
        + "\n"
        + "      @media only screen and (min-width: 768px) {\n"
        + "        .templateContainer {\n"
        + "          width: 600px !important;\n"
        + "        }\n"
        + "      }\n"
        + "\n"
        + "      @media only screen and (max-width: 480px) {\n"
        + "        body {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        table {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        td {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        p {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        a {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        li {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        blockquote {\n"
        + "          -webkit-text-size-adjust: none !important;\n"
        + "        }\n"
        + "\n"
        + "        body {\n"
        + "          width: 100% !important;\n"
        + "          min-width: 100% !important;\n"
        + "        }\n"
        + "\n"
        + "        #bodyCell {\n"
        + "          padding-top: 10px !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnImage {\n"
        + "          width: 100% !important;\n"
        + "          /* background-color: rgb(68,166,198); */\n"
        + "        }\n"
        + "\n"
        + "        .mcnTextContentContainer {\n"
        + "          max-width: 100% !important;\n"
        + "          width: 100% !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnCaptionLeftContentOuter .mcnTextContent {\n"
        + "          padding-top: 9px !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnCaptionRightContentOuter .mcnTextContent {\n"
        + "          padding-top: 9px !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnCaptionBlockInner .mcnCaptionTopContent:last-child .mcnTextContent {\n"
        + "          padding-top: 18px !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnTextContent {\n"
        + "          padding-right: 18px !important;\n"
        + "          padding-left: 18px !important;\n"
        + "        }\n"
        + "\n"
        + "        h1 {\n"
        + "          font-size: 22px !important;\n"
        + "          line-height: 125% !important;\n"
        + "        }\n"
        + "\n"
        + "        h2 {\n"
        + "          font-size: 20px !important;\n"
        + "          line-height: 125% !important;\n"
        + "        }\n"
        + "\n"
        + "        h3 {\n"
        + "          font-size: 18px !important;\n"
        + "          line-height: 125% !important;\n"
        + "        }\n"
        + "\n"
        + "        h4 {\n"
        + "          font-size: 16px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnBoxedTextContentContainer .mcnTextContent {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        .mcnBoxedTextContentContainer .mcnTextContent p {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templatePreheader {\n"
        + "          display: block !important;\n"
        + "        }\n"
        + "\n"
        + "        #templatePreheader .mcnTextContent {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templatePreheader .mcnTextContent p {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateHeader .mcnTextContent {\n"
        + "          font-size: 16px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateHeader .mcnTextContent p {\n"
        + "          font-size: 16px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateBody .mcnTextContent {\n"
        + "          font-size: 16px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateBody .mcnTextContent p {\n"
        + "          font-size: 16px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateFooter .mcnTextContent {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "\n"
        + "        #templateFooter .mcnTextContent p {\n"
        + "          font-size: 14px !important;\n"
        + "          line-height: 150% !important;\n"
        + "        }\n"
        + "      }\n"
        + "    </style>\n"
        + "  </head>\n"
        + "  <body\n"
        + "    style=\"\n"
        + "      height: 100%;\n"
        + "      width: 100%;\n"
        + "      -ms-text-size-adjust: 100%;\n"
        + "      -webkit-text-size-adjust: 100%;\n"
        + "      margin: 0;\n"
        + "      padding: 0;\n"
        + "    \"\n"
        + "    bgcolor=\"#FAFAFA\"\n"
        + "  >\n"
        + "    <!--*|IF:MC_PREVIEW_TEXT|*-->\n"
        + "    <span\n"
        + "      class=\"mcnPreviewText\"\n"
        + "      style=\"\n"
        + "        display: none !important;\n"
        + "        font-size: 0px;\n"
        + "        line-height: 0px;\n"
        + "        max-height: 0px;\n"
        + "        max-width: 0px;\n"
        + "        opacity: 0;\n"
        + "        overflow: hidden;\n"
        + "        visibility: hidden;\n"
        + "        mso-hide: all;\n"
        + "      \"\n"
        + "      > </span\n"
        + "    >\n"
        + "    <!--*|END:IF|*-->\n"
        + "    <center>\n"
        + "      <table\n"
        + "        align=\"center\"\n"
        + "        border=\"0\"\n"
        + "        cellpadding=\"0\"\n"
        + "        cellspacing=\"0\"\n"
        + "        height=\"100%\"\n"
        + "        width=\"100%\"\n"
        + "        id=\"bodyTable\"\n"
        + "        style=\"\n"
        + "          border-collapse: collapse;\n"
        + "          height: 100%;\n"
        + "          width: 100%;\n"
        + "          mso-table-lspace: 0pt;\n"
        + "          mso-table-rspace: 0pt;\n"
        + "          -ms-text-size-adjust: 100%;\n"
        + "          -webkit-text-size-adjust: 100%;\n"
        + "          margin: 0;\n"
        + "          padding: 0;\n"
        + "        \"\n"
        + "        bgcolor=\"#FAFAFA\"\n"
        + "      >\n"
        + "        <tr>\n"
        + "          <td\n"
        + "            align=\"center\"\n"
        + "            valign=\"top\"\n"
        + "            id=\"bodyCell\"\n"
        + "            style=\"\n"
        + "              height: 100%;\n"
        + "              width: 100%;\n"
        + "              mso-line-height-rule: exactly;\n"
        + "              -ms-text-size-adjust: 100%;\n"
        + "              -webkit-text-size-adjust: 100%;\n"
        + "              border-top-width: 0;\n"
        + "              margin: 0;\n"
        + "              padding: 10px;\n"
        + "            \"\n"
        + "          >\n"
        + "            <!-- BEGIN TEMPLATE // -->\n"
        + "            <!--[if (gte mso 9)|(IE)]>\n"
        + "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"600\" style=\"width:600px;\">\n"
        + "                    <tr>\n"
        + "                        <td align=\"center\" valign=\"top\" width=\"600\" style=\"width:600px;\">\n"
        + "                <![endif]-->\n"
        + "            <table\n"
        + "              border=\"0\"\n"
        + "              cellpadding=\"0\"\n"
        + "              cellspacing=\"0\"\n"
        + "              width=\"100%\"\n"
        + "              class=\"templateContainer\"\n"
        + "              style=\"\n"
        + "                border-collapse: collapse;\n"
        + "                mso-table-lspace: 0pt;\n"
        + "                mso-table-rspace: 0pt;\n"
        + "                -ms-text-size-adjust: 100%;\n"
        + "                -webkit-text-size-adjust: 100%;\n"
        + "                max-width: 600px !important;\n"
        + "                border: 0;\n"
        + "              \"\n"
        + "            >\n"
        + "              <tr>\n"
        + "                <td\n"
        + "                  valign=\"top\"\n"
        + "                  id=\"templatePreheader\"\n"
        + "                  style=\"\n"
        + "                    mso-line-height-rule: exactly;\n"
        + "                    -ms-text-size-adjust: 100%;\n"
        + "                    -webkit-text-size-adjust: 100%;\n"
        + "                    border-top-width: 0;\n"
        + "                    border-bottom-width: 0;\n"
        + "                    padding-top: 9px;\n"
        + "                    padding-bottom: 9px;\n"
        + "                    background: none no-repeat center / cover;\n"
        + "                  \"\n"
        + "                  bgcolor=\"#fafafa\"\n"
        + "                ></td>\n"
        + "              </tr>\n"
        + "              <tr>\n"
        + "                <td\n"
        + "                  valign=\"top\"\n"
        + "                  id=\"templateHeader\"\n"
        + "                  style=\"\n"
        + "                    mso-line-height-rule: exactly;\n"
        + "                    -ms-text-size-adjust: 100%;\n"
        + "                    -webkit-text-size-adjust: 100%;\n"
        + "                    border-top-width: 6px;\n"
        + "                    border-top-color: #4caad8;\n"
        + "                    border-top-style: none;\n"
        + "                    border-bottom-width: 0;\n"
        + "                    padding-top: 9px;\n"
        + "                    padding-bottom: 18px;\n"
        + "                    background: none no-repeat center / cover;\n"
        + "                  \"\n"
        + "                  bgcolor=\"#fafafa\"\n"
        + "                >\n"
        + "                  <table\n"
        + "                    border=\"0\"\n"
        + "                    cellpadding=\"0\"\n"
        + "                    cellspacing=\"0\"\n"
        + "                    width=\"100%\"\n"
        + "                    class=\"mcnImageBlock\"\n"
        + "                    style=\"\n"
        + "                      min-width: 100%;\n"
        + "                      border-collapse: collapse;\n"
        + "                      mso-table-lspace: 0pt;\n"
        + "                      mso-table-rspace: 0pt;\n"
        + "                      -ms-text-size-adjust: 100%;\n"
        + "                      -webkit-text-size-adjust: 100%;\n"
        + "                    \"\n"
        + "                  >\n"
        + "                    <tbody class=\"mcnImageBlockOuter\">\n"
        + "                      <tr>\n"
        + "                        <td\n"
        + "                          valign=\"top\"\n"
        + "                          style=\"\n"
        + "                            mso-line-height-rule: exactly;\n"
        + "                            -ms-text-size-adjust: 100%;\n"
        + "                            -webkit-text-size-adjust: 100%;\n"
        + "                            padding: 9px;\n"
        + "                          \"\n"
        + "                          class=\"mcnImageBlockInner\"\n"
        + "                        >\n"
        + "                          <table\n"
        + "                            align=\"left\"\n"
        + "                            width=\"100%\"\n"
        + "                            border=\"0\"\n"
        + "                            cellpadding=\"0\"\n"
        + "                            cellspacing=\"0\"\n"
        + "                            class=\"mcnImageContentContainer\"\n"
        + "                            style=\"\n"
        + "                              min-width: 100%;\n"
        + "                              border-collapse: collapse;\n"
        + "                              mso-table-lspace: 0pt;\n"
        + "                              mso-table-rspace: 0pt;\n"
        + "                              -ms-text-size-adjust: 100%;\n"
        + "                              -webkit-text-size-adjust: 100%;\n"
        + "                              background-color: #0075c9;\n"
        + "                            \"\n"
        + "                          >\n"
        + "                            <tbody>\n"
        + "                              <tr>\n"
        + "                                <td\n"
        + "                                  class=\"mcnImageContent\"\n"
        + "                                  valign=\"top\"\n"
        + "                                  style=\"\n"
        + "                                    mso-line-height-rule: exactly;\n"
        + "                                    -ms-text-size-adjust: 100%;\n"
        + "                                    -webkit-text-size-adjust: 100%;\n"
        + "                                    padding: 0 9px;\n"
        + "                                  \"\n"
        + "                                  align=\"center\"\n"
        + "                                >\n"
        + "                                  <a\n"
        + "                                    href=\"\"\n"
        + "                                    title=\"\"\n"
        + "                                    class=\"\"\n"
        + "                                    target=\"_blank\"\n"
        + "                                    style=\"\n"
        + "                                      mso-line-height-rule: exactly;\n"
        + "                                      -ms-text-size-adjust: 100%;\n"
        + "                                      -webkit-text-size-adjust: 100%;\n"
        + "                                    \"\n"
        + "                                  >\n"
        + "                                    <img\n"
        + "                                      align=\"center\"\n"
        + "                                      alt=\"\"\n"
        + "                                      src=\"https://www.blueskies.com/wp-content/uploads/2017/10/retina-01.png\"\n"
        + "                                      width=\"100\"\n"
        + "                                      style=\"\n"
        + "                                        max-width: 100px;\n"
        + "                                        padding-bottom: 0;\n"
        + "                                        display: inline !important;\n"
        + "                                        vertical-align: bottom;\n"
        + "                                        height: auto;\n"
        + "                                        outline: none;\n"
        + "                                        text-decoration: none;\n"
        + "                                        -ms-interpolation-mode: bicubic;\n"
        + "                                        border: 0;\n"
        + "                                      \"\n"
        + "                                      class=\"mcnImage\"\n"
        + "                                    />\n"
        + "                                  </a>\n"
        + "                                </td>\n"
        + "                              </tr>\n"
        + "                            </tbody>\n"
        + "                          </table>\n"
        + "                        </td>\n"
        + "                      </tr>\n"
        + "                    </tbody>\n"
        + "                  </table>\n"
        + "                </td>\n"
        + "              </tr>\n"
        + "              <tr>\n"
        + "                <td\n"
        + "                  valign=\"top\"\n"
        + "                  id=\"templateBody\"\n"
        + "                  style=\"\n"
        + "                    mso-line-height-rule: exactly;\n"
        + "                    -ms-text-size-adjust: 100%;\n"
        + "                    -webkit-text-size-adjust: 100%;\n"
        + "                    border-top-width: 6px;\n"
        + "                    border-top-color: #4caad8;\n"
        + "                    border-top-style: solid;\n"
        + "                    border-bottom-width: 2px;\n"
        + "                    border-bottom-color: #eaeaea;\n"
        + "                    border-bottom-style: solid;\n"
        + "                    padding-top: 9px;\n"
        + "                    padding-bottom: 9px;\n"
        + "                    background: none no-repeat center / cover;\n"
        + "                  \"\n"
        + "                  bgcolor=\"#ffffff\"\n"
        + "                >\n"
        + "                  <table\n"
        + "                    border=\"0\"\n"
        + "                    cellpadding=\"0\"\n"
        + "                    cellspacing=\"0\"\n"
        + "                    width=\"100%\"\n"
        + "                    class=\"mcnImageBlock\"\n"
        + "                    style=\"\n"
        + "                      min-width: 100%;\n"
        + "                      border-collapse: collapse;\n"
        + "                      mso-table-lspace: 0pt;\n"
        + "                      mso-table-rspace: 0pt;\n"
        + "                      -ms-text-size-adjust: 100%;\n"
        + "                      -webkit-text-size-adjust: 100%;\n"
        + "                    \"\n"
        + "                  >\n"
        + "                    <tbody class=\"mcnImageBlockOuter\">\n"
        + "                      <tr>\n"
        + "                        <td\n"
        + "                          valign=\"top\"\n"
        + "                          style=\"\n"
        + "                            mso-line-height-rule: exactly;\n"
        + "                            -ms-text-size-adjust: 100%;\n"
        + "                            -webkit-text-size-adjust: 100%;\n"
        + "                            padding: 9px;\n"
        + "                          \"\n"
        + "                          class=\"mcnImageBlockInner\"\n"
        + "                        >\n"
        + "                          <table\n"
        + "                            align=\"left\"\n"
        + "                            width=\"100%\"\n"
        + "                            border=\"0\"\n"
        + "                            cellpadding=\"0\"\n"
        + "                            cellspacing=\"0\"\n"
        + "                            class=\"mcnImageContentContainer\"\n"
        + "                            style=\"\n"
        + "                              min-width: 100%;\n"
        + "                              border-collapse: collapse;\n"
        + "                              mso-table-lspace: 0pt;\n"
        + "                              mso-table-rspace: 0pt;\n"
        + "                              -ms-text-size-adjust: 100%;\n"
        + "                              -webkit-text-size-adjust: 100%;\n"
        + "                            \"\n"
        + "                          >\n"
        + "                            <tbody>\n"
        + "                              <tr>\n"
        + "                                <td\n"
        + "                                  class=\"mcnImageContent\"\n"
        + "                                  valign=\"top\"\n"
        + "                                  style=\"\n"
        + "                                    mso-line-height-rule: exactly;\n"
        + "                                    -ms-text-size-adjust: 100%;\n"
        + "                                    -webkit-text-size-adjust: 100%;\n"
        + "                                    padding: 0 9px;\n"
        + "                                  \"\n"
        + "                                  align=\"center\"\n"
        + "                                >\n"
        + "                                  <img\n"
        + "                                    align=\"center\"\n"
        + "                                    alt=\"\"\n"
        + "                                    src=\"https://www.blueskies.com/wp-content/uploads/2020/01/Tagline-1.png\"\n"
        + "                                    width=\"150\"\n"
        + "                                    style=\"\n"
        + "                                      max-width: 150px;\n"
        + "                                      padding-bottom: 0;\n"
        + "                                      display: inline !important;\n"
        + "                                      vertical-align: bottom;\n"
        + "                                      height: auto;\n"
        + "                                      outline: none;\n"
        + "                                      text-decoration: none;\n"
        + "                                      -ms-interpolation-mode: bicubic;\n"
        + "                                      border: 0;\n"
        + "                                    \"\n"
        + "                                    class=\"mcnImage\"\n"
        + "                                  />\n"
        + "                                </td>\n"
        + "                              </tr>\n"
        + "                            </tbody>\n"
        + "                          </table>\n"
        + "                        </td>\n"
        + "                      </tr>\n"
        + "                    </tbody>\n"
        + "                  </table>\n"
        + "                  <table\n"
        + "                    border=\"0\"\n"
        + "                    cellpadding=\"0\"\n"
        + "                    cellspacing=\"0\"\n"
        + "                    width=\"100%\"\n"
        + "                    class=\"mcnTextBlock\"\n"
        + "                    style=\"\n"
        + "                      min-width: 100%;\n"
        + "                      border-collapse: collapse;\n"
        + "                      mso-table-lspace: 0pt;\n"
        + "                      mso-table-rspace: 0pt;\n"
        + "                      -ms-text-size-adjust: 100%;\n"
        + "                      -webkit-text-size-adjust: 100%;\n"
        + "                    \"\n"
        + "                  >\n"
        + "                    <tbody class=\"mcnTextBlockOuter\">\n"
        + "                      <tr>\n"
        + "                        <td\n"
        + "                          valign=\"top\"\n"
        + "                          class=\"mcnTextBlockInner\"\n"
        + "                          style=\"\n"
        + "                            padding-top: 9px;\n"
        + "                            mso-line-height-rule: exactly;\n"
        + "                            -ms-text-size-adjust: 100%;\n"
        + "                            -webkit-text-size-adjust: 100%;\n"
        + "                          \"\n"
        + "                        >\n"
        + "                          <!--[if mso]>\n"
        + "                                        <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\"\n"
        + "                                               style=\"width:100%;\">\n"
        + "                                            <tr>\n"
        + "                                        <![endif]-->\n"
        + "\n"
        + "                          <!--[if mso]>\n"
        + "                                        <td valign=\"top\" width=\"600\" style=\"width:600px;\">\n"
        + "                                        <![endif]-->\n"
        + "                          <table\n"
        + "                            align=\"left\"\n"
        + "                            border=\"0\"\n"
        + "                            cellpadding=\"0\"\n"
        + "                            cellspacing=\"0\"\n"
        + "                            style=\"\n"
        + "                              max-width: 100%;\n"
        + "                              min-width: 100%;\n"
        + "                              border-collapse: collapse;\n"
        + "                              mso-table-lspace: 0pt;\n"
        + "                              mso-table-rspace: 0pt;\n"
        + "                              -ms-text-size-adjust: 100%;\n"
        + "                              -webkit-text-size-adjust: 100%;\n"
        + "                            \"\n"
        + "                            width=\"100%\"\n"
        + "                            class=\"mcnTextContentContainer\"\n"
        + "                          >\n"
        + "                            <tbody>\n"
        + "                              <tr>\n"
        + "                                <td\n"
        + "                                  valign=\"top\"\n"
        + "                                  class=\"mcnTextContent\"\n"
        + "                                  style=\"\n"
        + "                                    mso-line-height-rule: exactly;\n"
        + "                                    -ms-text-size-adjust: 100%;\n"
        + "                                    -webkit-text-size-adjust: 100%;\n"
        + "                                    word-break: break-word;\n"
        + "                                    color: #202020;\n"
        + "                                    font-family: Helvetica;\n"
        + "                                    font-size: 16px;\n"
        + "                                    line-height: 150%;\n"
        + "                                    padding: 0 18px 9px;\n"
        + "                                  \"\n"
        + "                                  align=\"left\"\n"
        + "                                >\n"
        + "                                  <h1\n"
        + "                                    class=\"null\"\n"
        + "                                    style=\"\n"
        + "                                      display: block;\n"
        + "                                      color: #202020;\n"
        + "                                      font-family: Helvetica;\n"
        + "                                      font-size: 26px;\n"
        + "                                      font-style: normal;\n"
        + "                                      font-weight: bold;\n"
        + "                                      line-height: 125%;\n"
        + "                                      letter-spacing: normal;\n"
        + "                                      margin: 0;\n"
        + "                                      padding: 0;\n"
        + "                                    \"\n"
        + "                                    align=\"center\"\n"
        + "                                  >\n"
        + "                                    "
        + message
        + "                                    <br />\n"
        + "                                     \n"
        + "                                  </h1>\n"
        + "\n"
        + "                                  <div style=\"text-align: center\">\n"
        + "                                    <br />\n"
        + "                                    <table\n"
        + "                                      class=\"styled-table\"\n"
        + "                                      style=\"background-color: #fafafa\"\n"
        + "                                      align=\"center\"\n"
        + "                                    >\n"
        + table
        + "                                     </tr>\n"
        + "                                    </table>\n"
        + "                                    &nbsp;<br />\n"
        + "                                    &nbsp;\n"
        + "                                  </div>\n"
        + "                                </td>\n"
        + "                              </tr>\n"
        + "                            </tbody>\n"
        + "                          </table>\n"
        + "                          <!--[if mso]>\n"
        + "                                        </td>\n"
        + "                                        <![endif]-->\n"
        + "\n"
        + "                          <!--[if mso]>\n"
        + "                                        </tr>\n"
        + "                                        </table>\n"
        + "                                        <![endif]-->\n"
        + "                        </td>\n"
        + "                      </tr>\n"
        + "                    </tbody>\n"
        + "                  </table>\n"
        + "                  <table\n"
        + "                    border=\"0\"\n"
        + "                    cellpadding=\"0\"\n"
        + "                    cellspacing=\"0\"\n"
        + "                    width=\"100%\"\n"
        + "                    class=\"mcnButtonBlock\"\n"
        + "                    style=\"\n"
        + "                      min-width: 100%;\n"
        + "                      border-collapse: collapse;\n"
        + "                      mso-table-lspace: 0pt;\n"
        + "                      mso-table-rspace: 0pt;\n"
        + "                      -ms-text-size-adjust: 100%;\n"
        + "                      -webkit-text-size-adjust: 100%;\n"
        + "                    \"\n"
        + "                  >\n"
        + "                    <tbody class=\"mcnButtonBlockOuter\">\n"
        + "                      <tr>\n"
        + "                        <td\n"
        + "                          style=\"\n"
        + "                            mso-line-height-rule: exactly;\n"
        + "                            -ms-text-size-adjust: 100%;\n"
        + "                            -webkit-text-size-adjust: 100%;\n"
        + "                            padding: 0 18px 18px;\n"
        + "                          \"\n"
        + "                          valign=\"top\"\n"
        + "                          align=\"center\"\n"
        + "                          class=\"mcnButtonBlockInner\"\n"
        + "                        >\n"
        + "                          <table\n"
        + "                            border=\"0\"\n"
        + "                            cellpadding=\"0\"\n"
        + "                            cellspacing=\"0\"\n"
        + "                            class=\"mcnButtonContentContainer\"\n"
        + "                            style=\"\n"
        + "                              border-collapse: separate !important;\n"
        + "                              border-radius: 3px;\n"
        + "                              mso-table-lspace: 0pt;\n"
        + "                              mso-table-rspace: 0pt;\n"
        + "                              -ms-text-size-adjust: 100%;\n"
        + "                              -webkit-text-size-adjust: 100%;\n"
        + "                            \"\n"
        + "                            bgcolor=\"#2BAADF\"\n"
        + "                          >\n"
        + "                            <tbody>\n"
        + "                              <tr></tr>\n"
        + "                            </tbody>\n"
        + "                          </table>\n"
        + "                        </td>\n"
        + "                      </tr>\n"
        + "                    </tbody>\n"
        + "                  </table>\n"
        + "                </td>\n"
        + "              </tr>\n"
        + "              <tr></tr>\n"
        + "            </table>\n"
        + "\n"
        + "            <!--[if (gte mso 9)|(IE)]>\n"
        + "                </td>\n"
        + "                </tr>\n"
        + "                </table>\n"
        + "                <![endif]-->\n"
        + "            <!-- // END TEMPLATE -->\n"
        + "          </td>\n"
        + "        </tr>\n"
        + "      </table>\n"
        + "    </center>\n"
        + "  </body>\n"
        + "</html>\n";
  }
}
