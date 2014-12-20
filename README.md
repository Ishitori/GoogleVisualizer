GoogleVisualizer
================

Allows to send table data to google spreadsheet working under service account and your corporate domain. It is written on Java, since .NET version of the google spreadsheet client library does not support service account authorization. Service account authoriazation is required, if you want to update your google spreadsheets automatically, via a background process.

To see thow it works provide your p12 key and service account data and start Main().

If you want to use C# version of the library, you will have to convert .jar to .NET dll via IKVM project - http://www.ikvm.net/ Install IKVM and use the out/artifacts/GoogleVisualizer_jar/ConvertToDll.bat
