package com.example.midterm.utils;

import android.content.Context;
import android.os.Environment;

import com.example.midterm.model.entity.Ticket;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportUtils {

    /**
     * Export attendee list to Excel file
     */
    public static String exportAttendeesToExcel(Context context, String eventName, List<AttendeeData> attendees) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Danh sách người tham dự");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Họ tên", "Email", "Số điện thoại", "Loại vé", "Mã QR", "Ngày mua", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (AttendeeData attendee : attendees) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(attendee.name);
                row.createCell(2).setCellValue(attendee.email);
                row.createCell(3).setCellValue(attendee.phone);
                row.createCell(4).setCellValue(attendee.ticketType);
                row.createCell(5).setCellValue(attendee.qrCode);
                row.createCell(6).setCellValue(attendee.purchaseDate);
                row.createCell(7).setCellValue(attendee.status);
                rowNum++;
            }

            sheet.setColumnWidth(0, 2000);
            sheet.setColumnWidth(1, 6000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 6000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 4000);

            // Save file
            String fileName = "attendees_" + sanitizeFileName(eventName) + "_" + getTimestamp() + ".xlsx";
            File file = getExportFile(context, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Export sales report to Excel file
     */
    public static String exportSalesReportToExcel(Context context, String eventName, List<SalesData> salesData, SalesSummary summary) {
        try {
            Workbook workbook = new XSSFWorkbook();

            // Sheet 1: Summary
            Sheet summarySheet = workbook.createSheet("Tổng quan");
            createSummarySheet(summarySheet, workbook, eventName, summary);

            // Sheet 2: Detailed sales
            Sheet detailSheet = workbook.createSheet("Chi tiết bán vé");
            createSalesDetailSheet(detailSheet, workbook, salesData);

            // Save file
            String fileName = "sales_report_" + sanitizeFileName(eventName) + "_" + getTimestamp() + ".xlsx";
            File file = getExportFile(context, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createSummarySheet(Sheet sheet, Workbook workbook, String eventName, SalesSummary summary) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO BÁN VÉ - " + eventName);
        titleCell.setCellStyle(titleStyle);

        rowNum++; // Empty row

        String[][] summaryData = {
                {"Tổng số vé đã bán", String.valueOf(summary.totalTicketsSold)},
                {"Tổng doanh thu", String.format(Locale.getDefault(), "%,.0f VNĐ", summary.totalRevenue)},
                {"Số vé đã check-in", String.valueOf(summary.totalCheckedIn)},
                {"Số vé chưa check-in", String.valueOf(summary.totalPending)},
                {"Số vé đã hủy", String.valueOf(summary.totalCancelled)},
                {"Tỷ lệ lấp đầy", String.format(Locale.getDefault(), "%.1f%%", summary.fillRate)}
        };

        for (String[] data : summaryData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data[0]);
            row.createCell(1).setCellValue(data[1]);
        }

        // Set column widths manually (autoSizeColumn requires java.awt which is not available on Android)
        sheet.setColumnWidth(0, 8000);  // ~40 characters
        sheet.setColumnWidth(1, 6000);  // ~30 characters
    }

    private static void createSalesDetailSheet(Sheet sheet, Workbook workbook, List<SalesData> salesData) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Loại vé", "Giá", "Số lượng bán", "Doanh thu", "Tỷ lệ bán"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (SalesData data : salesData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(data.ticketType);
            row.createCell(1).setCellValue(String.format(Locale.getDefault(), "%,.0f VNĐ", data.price));
            row.createCell(2).setCellValue(data.soldQuantity + "/" + data.totalQuantity);
            row.createCell(3).setCellValue(String.format(Locale.getDefault(), "%,.0f VNĐ", data.revenue));
            row.createCell(4).setCellValue(String.format(Locale.getDefault(), "%.1f%%", data.sellRate));
        }

        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 3000);
    }

    /**
     * Export attendee list to PDF file
     */
    public static String exportAttendeesToPDF(Context context, String eventName, List<AttendeeData> attendees) {
        try {
            String fileName = "attendees_" + sanitizeFileName(eventName) + "_" + getTimestamp() + ".pdf";
            File file = getExportFile(context, fileName);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph title = new Paragraph("DANH SÁCH NGƯỜI THAM DỰ")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph eventTitle = new Paragraph(eventName)
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(eventTitle);

            document.add(new Paragraph("\n"));

            // Table
            float[] columnWidths = {1, 3, 3, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header
            String[] headers = {"STT", "Họ tên", "Email", "Loại vé", "Ngày mua", "Trạng thái"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold());
                table.addHeaderCell(cell);
            }

            // Data
            int index = 1;
            for (AttendeeData attendee : attendees) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index++))));
                table.addCell(new Cell().add(new Paragraph(attendee.name)));
                table.addCell(new Cell().add(new Paragraph(attendee.email)));
                table.addCell(new Cell().add(new Paragraph(attendee.ticketType)));
                table.addCell(new Cell().add(new Paragraph(attendee.purchaseDate)));
                table.addCell(new Cell().add(new Paragraph(attendee.status)));
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("\nXuất ngày: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Export sales report to PDF file
     */
    public static String exportSalesReportToPDF(Context context, String eventName, List<SalesData> salesData, SalesSummary summary) {
        try {
            // Handle null eventName
            if (eventName == null || eventName.trim().isEmpty()) {
                eventName = "Sự kiện";
            }

            String fileName = "sales_report_" + sanitizeFileName(eventName) + "_" + getTimestamp() + ".pdf";
            File file = getExportFile(context, fileName);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            Paragraph title = new Paragraph("BÁO CÁO BÁN VÉ")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            Paragraph eventTitle = new Paragraph(eventName)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(eventTitle);

            document.add(new Paragraph("\n"));

            // Summary section
            Paragraph summaryTitle = new Paragraph("TỔNG QUAN")
                    .setFontSize(14)
                    .setBold();
            document.add(summaryTitle);

            document.add(new Paragraph("Tổng số vé đã bán: " + summary.totalTicketsSold));
            document.add(new Paragraph("Tổng doanh thu: " + String.format(Locale.getDefault(), "%,.0f VNĐ", summary.totalRevenue)));
            document.add(new Paragraph("Số vé đã check-in: " + summary.totalCheckedIn));
            document.add(new Paragraph("Số vé chưa check-in: " + summary.totalPending));
            document.add(new Paragraph("Số vé đã hủy: " + summary.totalCancelled));
            document.add(new Paragraph("Tỷ lệ lấp đầy: " + String.format(Locale.getDefault(), "%.1f%%", summary.fillRate)));

            document.add(new Paragraph("\n"));

            // Detailed sales table
            Paragraph detailTitle = new Paragraph("CHI TIẾT BÁN VÉ THEO LOẠI")
                    .setFontSize(14)
                    .setBold();
            document.add(detailTitle);

            document.add(new Paragraph("\n"));

            // Create table with 5 columns
            float[] columnWidths = {3, 2, 2, 3, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header
            String[] headers = {"Loại vé", "Giá", "Số lượng", "Doanh thu", "Tỷ lệ"};
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setBold())
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            // Data rows
            for (SalesData data : salesData) {
                table.addCell(new Cell().add(new Paragraph(data.ticketType)));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%,.0f", data.price)))
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(data.soldQuantity + "/" + data.totalQuantity))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%,.0f", data.revenue)))
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(String.format(Locale.getDefault(), "%.1f%%", data.sellRate)))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            document.add(table);

            // Footer
            document.add(new Paragraph("\n\nXuất ngày: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Export tickets to PDF with QR codes
     */
    public static String exportTicketsToPDF(Context context, String eventName, List<Ticket> tickets) {
        try {
            String fileName = "tickets_" + sanitizeFileName(eventName) + "_" + getTimestamp() + ".pdf";
            File file = getExportFile(context, fileName);

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            for (Ticket ticket : tickets) {
                // Ticket header
                Paragraph header = new Paragraph("VÉ SỰ KIỆN")
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER);
                document.add(header);

                document.add(new Paragraph(eventName)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER));

                document.add(new Paragraph("\n"));

                // Ticket info
                document.add(new Paragraph("Mã vé: " + ticket.getQrCode()));
                document.add(new Paragraph("Ngày mua: " + ticket.getPurchaseDate()));
                document.add(new Paragraph("Trạng thái: " + ticket.getStatus()));

                document.add(new Paragraph("\n\n"));

                // Page break for next ticket
                if (tickets.indexOf(ticket) < tickets.size() - 1) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }
            }

            document.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static File getExportFile(Context context, String fileName) {
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        return new File(exportDir, fileName);
    }

    private static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "export";
        }
        return name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }

    // Data classes
    public static class AttendeeData {
        public String name;
        public String email;
        public String phone;
        public String ticketType;
        public String qrCode;
        public String purchaseDate;
        public String status;

        public AttendeeData(String name, String email, String phone, String ticketType,
                           String qrCode, String purchaseDate, String status) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.ticketType = ticketType;
            this.qrCode = qrCode;
            this.purchaseDate = purchaseDate;
            this.status = status;
        }
    }

    public static class SalesData {
        public String ticketType;
        public double price;
        public int soldQuantity;
        public int totalQuantity;
        public double revenue;
        public double sellRate;

        public SalesData(String ticketType, double price, int soldQuantity, int totalQuantity) {
            this.ticketType = ticketType;
            this.price = price;
            this.soldQuantity = soldQuantity;
            this.totalQuantity = totalQuantity;
            this.revenue = price * soldQuantity;
            this.sellRate = totalQuantity > 0 ? (soldQuantity * 100.0 / totalQuantity) : 0;
        }
    }

    public static class SalesSummary {
        public int totalTicketsSold;
        public double totalRevenue;
        public int totalCheckedIn;
        public int totalPending;
        public int totalCancelled;
        public double fillRate;

        public SalesSummary(int totalTicketsSold, double totalRevenue, int totalCheckedIn,
                           int totalPending, int totalCancelled, int totalCapacity) {
            this.totalTicketsSold = totalTicketsSold;
            this.totalRevenue = totalRevenue;
            this.totalCheckedIn = totalCheckedIn;
            this.totalPending = totalPending;
            this.totalCancelled = totalCancelled;
            this.fillRate = totalCapacity > 0 ? (totalTicketsSold * 100.0 / totalCapacity) : 0;
        }
    }
}
