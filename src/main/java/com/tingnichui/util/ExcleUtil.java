package com.tingnichui.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ExcleUtil {
    private ExcleUtil() {
    }

    public static void main(String[] args) throws Exception {
//        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\abc\\Documents\\table.xls");
        File srcFile = new File("C:\\Users\\abc\\Documents\\table.xls");
        File destFile = new File("C:\\Users\\abc\\Documents\\tt.xls");
        FileUtils.copyFile(srcFile, destFile);
        System.err.println(readExcelFile(destFile));
        System.err.println(destFile.delete());

//        System.err.println(readExcelFile(new File("C:\\Users\\abc\\Documents\\table.xls")));

    }

    public static List<Map<String,String>> readExcelFile(File file) throws Exception{
        Workbook workbook;
        List<Map<String,String>> excelList = new ArrayList<>();


        //判断什么类型文件
        if (file.getName().endsWith(".xls")) {
            POIFSFileSystem ps = new POIFSFileSystem(file);
            workbook = new HSSFWorkbook(ps);
        } else if (file.getName().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(file);
        } else {
            return excelList;
        }

        //获取第一个sheet
        Sheet sheet = workbook.getSheetAt(0);
        if(sheet == null) return excelList;
        //获取一个sheet有多少Row
        int rowCount = sheet.getLastRowNum();
        if(rowCount == 0) return excelList;
        Row firstRow = sheet.getRow(0);
        Row row = null;
        for (int i = 1; i <= rowCount; i++) {
            row = sheet.getRow(i);
            if(row == null) continue;
            Map<String,String> cellMap = new HashMap<>();;
            for (int j = 0; j <= firstRow.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                if (cell == null) continue;
                String value = null;
                switch (cell.getCellType()) {
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Date date = cell.getDateCellValue();
                            value = DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
                        } else {
                            cell.setCellType(CellType.STRING);
                            value = cell.getStringCellValue().trim();
                        }
                        break;
                    default:
                        value = cell.getStringCellValue().trim();
                        break;
                }
                cellMap.put(firstRow.getCell(j).getStringCellValue().trim(), value);
            }
            if(cellMap != null) excelList.add(cellMap);
        }
        return excelList;
    }


    public static File createExcel(List<List<String>> rows) throws IOException {
        Workbook workbook;
        FileOutputStream fos = null;
        try {
            workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            for (int i = 0; i < rows.size(); i++) {
                List<String> cells = rows.get(i);
                Row row = sheet.createRow(i);
                for (int j = 0; j < cells.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(cells.get(j));
                }
            }

            File file = File.createTempFile("excel_",".xlsx");
            fos = new FileOutputStream(file);

            workbook.write(fos);
            fos.flush();

            return file;
        } finally {
            if(fos != null) fos.close();
        }
    }
}
