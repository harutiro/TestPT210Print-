package net.harutiro.test_pt_210_print

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import com.android.print.sdk.Barcode
import com.android.print.sdk.CanvasPrint
import com.android.print.sdk.FontProperty
import com.android.print.sdk.PrinterConstants
import com.android.print.sdk.PrinterConstants.BarcodeType
import com.android.print.sdk.PrinterInstance
import com.android.print.sdk.PrinterType
import com.android.print.sdk.Table

class PrintUtils {
    fun printText(resources: Resources, mPrinter: PrinterInstance) {
        mPrinter.init()
        mPrinter.printText(resources.getString(R.string.example_text))
        // 换行
        // mPrinter.setPrinter(Command.PRINT_AND_NEWLINE);
        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2) // 换2行

        //切刀
        //mPrinter.cutPaper();
    }

    fun printNote(
        resources: Resources, mPrinter: PrinterInstance,
        is58mm: Boolean
    ) {
        mPrinter.init()
        var sb = StringBuffer()

        // mPrinter.setPrinter(BluetoothPrinter.COMM_LINE_HEIGHT, 80);
        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER)
        // 字号横向纵向扩大一倍
        mPrinter.setCharacterMultiple(1, 1)
        mPrinter.printText(
            resources.getString(R.string.shop_company_title)
                    + "\n"
        )

        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_LEFT)
        // 字号使用默认
        mPrinter.setCharacterMultiple(0, 0)
        sb.append(resources.getString(R.string.shop_num) + "574001\n")
        sb.append(
            (resources.getString(R.string.shop_receipt_num)
                    + "S00003169\n")
        )
        sb.append(
            (resources.getString(R.string.shop_cashier_num)
                    + "s004_s004\n")
        )

        sb.append(
            (resources.getString(R.string.shop_receipt_date)
                    + "2012-06-17\n")
        )
        sb.append(
            (resources.getString(R.string.shop_print_time)
                    + "2012-06-17 13:37:24\n")
        )
        mPrinter.printText(sb.toString()) // 打印

        printTable1(resources, mPrinter, is58mm) // 打印表格

        sb = StringBuffer()
        if (is58mm) {
            sb.append(
                (resources.getString(R.string.shop_goods_number)
                        + "                6.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_goods_total_price)
                        + "                35.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_payment)
                        + "                100.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_change)
                        + "                65.00\n")
            )
        } else {
            sb.append(
                (resources.getString(R.string.shop_goods_number)
                        + "                                6.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_goods_total_price)
                        + "                                35.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_payment)
                        + "                                100.00\n")
            )
            sb.append(
                (resources.getString(R.string.shop_change)
                        + "                                65.00\n")
            )
        }

        sb.append(resources.getString(R.string.shop_company_name) + "\n")
        sb.append(
            (resources.getString(R.string.shop_company_site)
                    + "www.jiangsu1510.com\n")
        )
        sb.append(resources.getString(R.string.shop_company_address) + "\n")
        sb.append(
            (resources.getString(R.string.shop_company_tel)
                    + "0574-88222999\n")
        )
        sb.append(
            (resources.getString(R.string.shop_Service_Line)
                    + "4008-567-567 \n")
        )
        if (is58mm) {
            sb.append("==============================\n")
        } else {
            sb.append("==============================================\n")
        }
        mPrinter.printText(sb.toString())

        mPrinter.setPrinter(PrinterConstants.Command.ALIGN, PrinterConstants.Command.ALIGN_CENTER)
        mPrinter.setCharacterMultiple(0, 1)
        mPrinter.printText(resources.getString(R.string.shop_thanks) + "\n")
        mPrinter.printText(resources.getString(R.string.shop_demo) + "\n\n\n")

        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
    }

    fun printImage(resources: Resources?, mPrinter: PrinterInstance) {
        mPrinter.init()
        val bitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_launcher_foreground
        )
        // getCanvasImage方法获得画布上所画的图像,printImage方法打印图像.
        mPrinter.printText("Print Image:\n")
        mPrinter.printImage(bitmap)
        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2) // 换2行
    }

    fun printCustomImage(
        resources: Resources?,
        mPrinter: PrinterInstance, isStylus: Boolean, is58mm: Boolean
    ) {
        mPrinter.init()

        // TODO Auto-generated method stub
        val cp = CanvasPrint()
        /*
		 * 初始化画布，画布的宽度为变量，一般有两个选择： 1、58mm型号打印机实际可用是48mm，48*8=384px
		 * 2、80mm型号打印机实际可用是72mm，72*8=576px 因为画布的高度是无限制的，但从内存分配方面考虑要小于4M比较合适，
		 * 所以预置为宽度的5倍。 初始化画笔，默认属性有： 1、消除锯齿 2、设置画笔颜色为黑色
		 */
        // init 方法包含cp.initCanvas(550)和cp.initPaint(), T9打印宽度为72mm,其他为47mm.
        if (isStylus) {
            cp.init(PrinterType.T5)
        } else {
            if (is58mm) {
                cp.init(PrinterType.TIII)
            } else {
                cp.init(PrinterType.T9)
            }
        }

        // 非中文使用空格分隔单词
        cp.setUseSplit(true)
        //cp.setUseSplitAndString(true, " ");
        // 阿拉伯文靠右显示
        cp.setTextAlignRight(true)
        /*
		 * 插入图片函数: drawImage(float x, float y, String path)
		 * 其中(x,y)是指插入图片的左上顶点坐标。
		 */
        val fp = FontProperty()
        fp.setFont(false, false, false, false, 25, null)
        // 通过初始化的字体属性设置画笔
        cp.setFontProperty(fp)
        cp.drawText("Contains Arabic language:")
        // pg.drawText("温度的影响主要表现在两个方面温度的影响主要表现在两个方面温度的影响主要表现在两个方面温度的影响主要表现在两个方面");
        fp.setFont(false, false, false, false, 30, null)
        cp.setFontProperty(fp)
        cp.drawText("ومن تكهناته إيمانه بإستحالة قياس السرعة اللحظية للجسيمات متناهية الصغر والتي تهتز عشوائياً في مختلف الإتجاهات بما يعرف باسم الحركة البراونية، لكن بعد قرن من الزمان، تمكن عالم يدعى مارك رايزن من تفنيد هذه المقولة عملياً بمعمل أبحاثه بجامعة تكساس وإستطاع قياس السرعة اللحظية لتلك الجسيمات، في خضم إختباراته لقانون التوزع المتساوي الذي يقرر أن طاقة حركة الجسيم تعتمد على حرارته بشكل بحت وليس على على كتلته أو حجمه، ")
        cp.drawImage(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_launcher_foreground
            )
        )

        mPrinter.printText("Print Custom Image:\n")
        mPrinter.printImage(cp.canvasImage)

        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
    }

    fun printTable(
        resources: Resources,
        mPrinter: PrinterInstance, is58mm: Boolean
    ) {
        mPrinter.init()
        // getTable方法:参数1,以特定符号分隔的列名; 2,列名分隔符;
        // 3,各列所占字符宽度,中文2个,英文1个. 默认字体总共不要超过48
        // 表格超出部分会另起一行打印.若想手动换行,可加\n.
        mPrinter.setCharacterMultiple(0, 0)
        val column = resources.getString(R.string.note_title)
        val table: Table
        if (is58mm) {
            table = Table(column, ";", intArrayOf(14, 6, 6, 6))
        } else {
            table = Table(column, ";", intArrayOf(16, 8, 8, 12))
        }

        table.setColumnAlignRight(true)
        table.addRow(
            ("1," + resources.getString(R.string.coffee)
                    + ";2.00;5.00;10.00")
        )
        table.addRow(
            ("2," + resources.getString(R.string.tableware)
                    + ";2.00;5.00;10.00")
        )
        table.addRow(
            ("3," + resources.getString(R.string.frog)
                    + ";1.00;68.00;68.00")
        )
        table.addRow(
            ("4," + resources.getString(R.string.cucumber)
                    + ";1.00;4.00;4.00")
        )
        table.addRow(
            ("5," + resources.getString(R.string.peanuts)
                    + "; 1.00;5.00;5.00")
        )
        table.addRow(
            ("6," + resources.getString(R.string.rice)
                    + ";1.00;2.00;2.00")
        )
        mPrinter.printTable(table)

        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 2)
    }

    fun printTable1(
        resources: Resources,
        mPrinter: PrinterInstance, is58mm: Boolean
    ) {
        mPrinter.init()
        val column = resources.getString(R.string.note_title)
        val table: Table
        if (is58mm) {
            table = Table(column, ";", intArrayOf(14, 6, 6, 6))
        } else {
            table = Table(column, ";", intArrayOf(18, 10, 10, 12))
        }
        table.addRow("" + resources.getString(R.string.bags) + ";10.00;1;10.00")
        table.addRow("" + resources.getString(R.string.hook) + ";5.00;2;10.00")
        table.addRow(
            ("" + resources.getString(R.string.umbrella)
                    + ";5.00;3;15.00")
        )
        mPrinter.printTable(table)
    }

    fun printBarCode(mPrinter: PrinterInstance) {
        mPrinter.init()
        mPrinter.setCharacterMultiple(0, 0)
        /**
         * 设置左边距,nL,nH 设置宽度为(nL+nH*256)* 横向移动单位. 设置左边距对打印条码的注释位置有影响.
         */
        mPrinter.setLeftMargin(15, 0)

        // mPrinter.setPrinter(BluetoothPrinter.COMM_ALIGN,BluetoothPrinter.COMM_ALIGN_LEFT);
        /**
         * 参数1: 设置条码横向宽度 2<=n<=6,默认为2 参数2: 设置条码高度 1<=n<=255,默认162 参数3:
         * 设置条码注释打印位置.0不打印,1上方,2下方,3上下方均有,默认为0 参数4:
         * 设置条码类型.BluetoothPrinter.BAR_CODE_TYPE_ 开头的常量,默认为CODE128
         */
        var barcode: Barcode
        // upc_a 123456789012
        mPrinter.printText("UPC_A\n")
        barcode = Barcode(BarcodeType.UPC_A, 2, 150, 2, "123456789012")
        mPrinter.printBarCode(barcode)

        // upc-e暂时规则不知道。。。。
        // mPrinter.printText("UPC_E\n");
        // barcode = new Barcode(BarcodeType.UPC_E, 2, 150, 2, "123456");
        // mPrinter.printBarCode(barcode);
        mPrinter.printText("JAN13(EAN13)\n")
        barcode = Barcode(BarcodeType.JAN13, 2, 150, 2, "123456789012")
        mPrinter.printBarCode(barcode)

        // JAN8(EAN8) 1234567
        mPrinter.printText("JAN8(EAN8)\n")
        barcode = Barcode(BarcodeType.JAN8, 2, 150, 2, "1234567")
        mPrinter.printBarCode(barcode)

        // "CODE39"
        mPrinter.printText("CODE39\n")
        barcode = Barcode(BarcodeType.CODE39, 2, 150, 2, "123456")
        mPrinter.printBarCode(barcode)

        // ITF
        mPrinter.printText("ITF\n")
        barcode = Barcode(BarcodeType.ITF, 2, 150, 2, "123456")
        mPrinter.printBarCode(barcode)

        // CODABAR
        mPrinter.printText("CODABAR\n")
        barcode = Barcode(BarcodeType.CODABAR, 2, 150, 2, "123456")
        mPrinter.printBarCode(barcode)
        // CODE93
        mPrinter.printText("CODE93\n")
        barcode = Barcode(BarcodeType.CODE93, 2, 150, 2, "123456")
        mPrinter.printBarCode(barcode)

        // Code128
        mPrinter.printText("CODE128\n")
        barcode = Barcode(BarcodeType.CODE128, 2, 150, 2, "No.123456")
        mPrinter.printBarCode(barcode)

        // ========
        // "PDF417"
        mPrinter.printText("PDF417\n")
        barcode = Barcode(BarcodeType.PDF417, 2, 3, 6, "No.123456")
        mPrinter.printBarCode(barcode)
        // "DATAMATRIX"
        mPrinter.printText("DATAMATRIX\n")
        barcode = Barcode(BarcodeType.DATAMATRIX, 2, 3, 6, "No.123456")
        mPrinter.printBarCode(barcode)
        // "QRCODE"
        mPrinter.printText("QRCODE\n")
        barcode = Barcode(BarcodeType.QRCODE, 2, 3, 6, "No.123456")
        mPrinter.printBarCode(barcode)

        mPrinter.setPrinter(PrinterConstants.Command.PRINT_AND_WAKE_PAPER_BY_LINE, 1)
    }

    /**
     * 将彩色图转换为黑白图
     *
     * @param 位图
     * @return 返回转换好的位图
     */
    fun convertToBlackWhite(bmp: Bitmap): Bitmap {
        val width = bmp.width // 获取位图的宽
        val height = bmp.height // 获取位图的高
        val pixels = IntArray(width * height) // 通过位图的大小创建像素点数组

        bmp.getPixels(pixels, 0, width, 0, 0, width, height)
        val alpha = 0xFF shl 24
        for (i in 0 until height) {
            for (j in 0 until width) {
                var grey = pixels[width * i + j]

                val red = ((grey and 0x00FF0000) shr 16)
                val green = ((grey and 0x0000FF00) shr 8)
                val blue = (grey and 0x000000FF)

                grey = ((red * 0.3) + (green * 0.59) + (blue * 0.11)).toInt()
                grey = alpha or (grey shl 16) or (grey shl 8) or grey
                pixels[width * i + j] = grey
            }
        }
        val newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height)

        val resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, 380, 460)
        return resizeBmp
    }
}
