package com.logistics.supply.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@UtilityClass
public class LpoTemplate {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("EEEEE dd MMMMM yyyy").withLocale(Locale.UK);

  private static final String TEMPLATE = """
      <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional //EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
      <html
      \txmlns="http://www.w3.org/1999/xhtml"
      \txmlns:th="http://www.thymeleaf.org" lang="en" xml:lang="en">
      \t<head>
      \t\t<title>LOCAL ORDER REQUISITION</title>
      \t\t<meta name="x-apple-disable-message-reformatting" />
      \t\t<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      \t\t<meta http-equiv="X-UA-Compatible" content="IE=edge" />
      \t\t<meta name="viewport" content="width=device-width, initial-scale=1.0" />
      \t\t<meta name="x-apple-disable-message-reformatting" />
      \t\t<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      \t\t<meta http-equiv="X-UA-Compatible" content="IE=edge" />
      \t\t<meta name="viewport" content="width=device-width, initial-scale=1.0" />
      \t\t<base href="https://preview.hs-sites.com" target="_blank" />
      \t\t<meta name="generator" content="HubSpot" />
      \t\t<meta property="og:url" content="http://oze-4216469.hs-sites.com/your-digital-receipt-from-tozé-business-app?hs_preview=URDbBNaP-36188940965" />
      \t\t<meta name="robots" content="noindex,follow" />
      \t\t<style>@font-face {
      font-family: 'Lato'; font-style: italic; font-weight: 400; src: local('Lato Italic'), local('Lato-Italic'), url('https://fonts.gstatic.com/s/lato/v17/S6u8w4BMUTPHjxsAXC-v.ttf') format('truetype');
      }
      @font-face {
      font-family: 'Lato'; font-style: italic; font-weight: 700; src: local('Lato Bold Italic'), local('Lato-BoldItalic'), url('https://fonts.gstatic.com/s/lato/v17/S6u_w4BMUTPHjxsI5wq_Gwfo.ttf') format('truetype');
      }
      @font-face {
      font-family: 'Lato'; font-style: normal; font-weight: 400; src: local('Lato Regular'), local('Lato-Regular'), url('https://fonts.gstatic.com/s/lato/v17/S6uyw4BMUTPHjx4wWw.ttf') format('truetype');
      }
      @font-face {
      font-family: 'Lato'; font-style: normal; font-weight: 700; src: local('Lato Bold'), local('Lato-Bold'), url('https://fonts.gstatic.com/s/lato/v17/S6u9w4BMUTPHh6UVSwiPHA.ttf') format('truetype');
      }
      .ExternalClass {
      width: 100%;
      }
      .ExternalClass {
      line-height: 100%;
      }
      body {
      -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%; -webkit-font-smoothing: antialiased; moz-osx-font-smoothing: grayscale;
      }
      @media only screen and (min-width:640px) {
        .hse-column-container {
          max-width: 600px !important; width: 600px !important;
        }
        .hse-column {
          display: table-cell; vertical-align: top;
        }
        .hse-section .hse-size-6 {
          max-width: 300px !important; width: 300px !important;
        }
        .hse-section .hse-size-12 {
          max-width: 600px !important; width: 600px !important;
        }
      }
      @media only screen and (max-width:639px) {
        img.stretch-on-mobile {
          height: auto !important; width: 100% !important;
        }
        .hs_rss_email_entries_table img {
          height: auto !important; width: 100% !important;
        }
        .display_block_on_small_screens {
          display: block;
        }
        .hs_padded {
          padding-left: 20px !important; padding-right: 20px !important;
        }
      }

      .styled-table {
              border-collapse: collapse;
              margin: 25px 0;
              font-size: 0.95em;
              font-family: HelveticaNeue-Light;
              min-width: 400px;
            }

            .styled-table thead tr {
              background-color: #009879;
              color: #ffffff;
              text-align: left;
            }

            .styled-table tbody tr {
              border-bottom: 1px solid #dddddd;
            }

            .styled-table tbody tr:nth-of-type(even) {
              background-color: #f3f3f3;
            }

            .styled-table tbody tr:last-of-type {
              border-bottom: 2px solid #0075c9;
            }

            .styled-table th,
            .styled-table td {
              padding: 12px 20px;
            }

      </style>
      \t</head>
      \t<body bgcolor="#EAF0F6" style="font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #0075c9; word-break: break-word; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%; -webkit-font-smoothing: antialiased; moz-osx-font-smoothing: grayscale; margin: 0; padding: 0;">
      \t\t<div style="display: none !important; font-size: 1px; color: #EAF0F6; line-height: 1px; max-height: 0px; max-width: 0px; opacity: 0; overflow: hidden;">LPO</div>
      \t\t<div class="hse-body-background" style="background-color: #eaf0f6;" bgcolor="#0075c9">
      \t\t\t<table role="presentation" class="hse-body-wrapper-table" cellpadding="0" cellspacing="0" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100% !important; min-width: 320px !important; height: 100% !important; margin: 0; padding: 0;" width="100%" height="100%">
      \t\t\t\t<tbody>
      \t\t\t\t\t<tr>
      \t\t\t\t\t\t<td class="hse-body-wrapper-td" valign="top" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding-top: 20px; ">
      \t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_email_flex_area" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="email_flex_area">
      \t\t\t\t\t\t\t\t<div class="hse-section hse-section-first" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; padding-bottom: 0px; padding-top: 0px; background: #0075c9 url('https://f.hubspotusercontent10.net/hubfs/4216469/gradiaent%20blue.jpg') no-repeat center / 100% 100%;">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t<table class="hse-image-wrapper" role="presentation" width="100%" cellpadding="0" cellspacing="0" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #0075c9;" >
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td align="center" valign="top" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; color: #0075c9; word-break: break-word; font-size: 0px; padding: 0px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<img alt="LPO" src="https://www.blueskies.com/wp-content/uploads/2017/10/retina-01.png" style="outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; max-width: 100%; font-size: 16px; vertical-align: middle;" width="100" />
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="center">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<strong>LOCAL PURCHASE ORDER</strong>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;">LPO ID: </p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="right">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<span th:text="${subscriptionPlan}" />{{LPO_ID}}
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 0px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table role="presentation" width="100%" align="center" border="0" style="position: relative; top: -1px; min-width: 20px; width: 100%; max-width: 100%; border-spacing: 0; mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; font-size: 1px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td width="100%" valign="middle" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; line-height: 0; border-bottom-style: solid; mso-border-bottom-alt: 1px solid #EEEEEE; border-bottom-width: 1px; border-color: transparent transparent #eeeeee;"></td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;">Supplier: </p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="right">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<span th:text="${receiptNumber}" />{{SUPPLIER}}\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 0px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table role="presentation" width="100%" align="center" border="0" style="position: relative; top: -1px; min-width: 20px; width: 100%; max-width: 100%; border-spacing: 0; mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; font-size: 1px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td width="100%" valign="middle" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; line-height: 0; border-bottom-style: solid; mso-border-bottom-alt: 1px solid #EEEEEE; border-bottom-width: 1px; border-color: transparent transparent #eeeeee;"></td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;">Date:</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="right">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<span th:text="${trxdate}" />{{DATE}}</span>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 0px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table role="presentation" width="100%" align="center" border="0" style="position: relative; top: -1px; min-width: 20px; width: 100%; max-width: 100%; border-spacing: 0; mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; font-size: 1px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td width="100%" valign="middle" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; line-height: 0; border-bottom-style: solid; mso-border-bottom-alt: 1px solid #EEEEEE; border-bottom-width: 1px; border-color: transparent transparent #eeeeee;"></td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;">Comment:</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-6">
      \t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="right">{{COMMENT}}</p>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; background-color: #ffffff;" bgcolor="#ffffff">
      \t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 0px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<table role="presentation" width="100%" align="center" border="0" style="position: relative; top: -1px; min-width: 20px; width: 100%; max-width: 100%; border-spacing: 0; mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-collapse: collapse; font-size: 1px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<td width="100%" valign="middle" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; line-height: 0; border-bottom-style: solid; mso-border-bottom-alt: 1px solid #EEEEEE; border-bottom-width: 1px; border-color: transparent transparent #eeeeee;"></td>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t<!-- <table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;"><tbody><tr><td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 40px 20px 10px;"><div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module"><div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_rich_text" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="rich_text"><p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="center">Questions about your receipt?</p><p style="mso-line-height-rule: exactly; line-height: 175%; margin: 0;" align="center"><a href="https://wa.me/233545750476" rel="noopener" style="mso-line-height-rule: exactly; color: #00a4bd; text-decoration: underline;" data-hs-link-id="0" target="_blank">Contact Coach</a></p></div></div></td></tr></tbody></table> -->
      \t\t\t\t\t\t\t\t\t\t<div style="text-align: center">
      \t\t\t\t\t\t\t\t\t\t\t<br />
      \t\t\t\t\t\t\t\t\t\t\t<table
                                              class="styled-table"
                                              style="background-color: #fafafa"
                                              align="center"
                                            >
      {{TABLE_HTML}}\t\t\t\t\t\t\t\t\t\t\t</table>
                                            &nbsp;
      \t\t\t\t\t\t\t\t\t\t\t<br />
                                            &nbsp;

      \t\t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t<div class="hse-section" style="padding-left: 10px; padding-right: 10px;">
      \t\t\t\t\t\t\t\t<div class="hse-column-container" style="min-width: 280px; max-width: 600px; width: 100%; margin-left: auto; margin-right: auto; border-collapse: collapse; border-spacing: 0; padding-bottom: 20px; padding-top: 15px;">
      \t\t\t\t\t\t\t\t\t<div class="hse-column hse-size-12">
      \t\t\t\t\t\t\t\t\t\t<table role="presentation" cellpadding="0" cellspacing="0" width="100%" style="border-spacing: 0 !important; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
      \t\t\t\t\t\t\t\t\t\t\t<tbody>
      \t\t\t\t\t\t\t\t\t\t\t\t<tr>
      \t\t\t\t\t\t\t\t\t\t\t\t\t<td class="hs_padded" style="border-collapse: collapse; mso-line-height-rule: exactly; font-family: Lato, Tahoma, sans-serif; font-size: 16px; color: #23496d; word-break: break-word; padding: 10px 20px;">
      \t\t\t\t\t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module"></div>
      \t\t\t\t\t\t\t\t\t\t\t\t\t</td>
      \t\t\t\t\t\t\t\t\t\t\t\t</tr>
      \t\t\t\t\t\t\t\t\t\t\t</tbody>
      \t\t\t\t\t\t\t\t\t\t</table>
      \t\t\t\t\t\t\t\t\t\t<div class="hs_cos_wrapper hs_cos_wrapper_widget hs_cos_wrapper_type_module" style="color: inherit; font-size: inherit; line-height: inherit;" data-hs-cos-general-type="widget" data-hs-cos-type="module"></div>
      \t\t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t\t</div>
      \t\t\t\t\t\t</td>
      \t\t\t\t\t</tr>
      \t\t\t\t</tbody>
      \t\t\t</table>
      \t\t</div>
      \t</body>
      </html>
      """;

  public String lpoCompose(int lpoId, String supplier, String comment, String tableHtml, LocalDate date) {
    String today = DATE_FORMATTER.format(date);
    return TEMPLATE
        .replace("{{LPO_ID}}", String.valueOf(lpoId))
        .replace("{{SUPPLIER}}", supplier)
        .replace("{{DATE}}", today)
        .replace("{{COMMENT}}", comment)
        .replace("{{TABLE_HTML}}", tableHtml);
  }

  public String lpoCompose(int lpoId, String supplier, String comment, String tableHtml) {
    return lpoCompose(lpoId, supplier, comment, tableHtml, LocalDate.now());
  }
}
