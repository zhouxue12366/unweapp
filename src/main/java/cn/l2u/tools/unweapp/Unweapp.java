package cn.l2u.tools.unweapp;

import java.io.*;
import java.util.ArrayList;

public class Unweapp {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("用法:\nunweapp 输入文件 [输出文件夹]");
        } else {
            File in = new File(args[0]);
            File outDir = args.length == 2 ? outDir = new File(args[1]) : new File(in.getAbsolutePath()+"_unpacked");
            run(in, outDir);
        }
    }

    private static void run(File in, File outDir) throws IOException {
        RandomAccessFile r = new RandomAccessFile(in, "r");
        try {
            if (r.readByte() != (byte) 0xBE) {
                throw new RuntimeException("文件类型错误");
            }
            r.seek(0xE);//前面几个字节不知道含义，估计是校验码之类的；不影响解包
            int fileCount = r.readInt();//文件数量
            ArrayList<WxapkgItem> wxapkgItems = new ArrayList<WxapkgItem>(fileCount);

            for (int i = 0; i < fileCount; i++) {
                int nameLen = r.readInt();//文件名长度
                byte[] buf = new byte[nameLen];
                r.read(buf, 0, nameLen);//文件名
                String name = new String(buf, 0, nameLen);
                int start = r.readInt();//文件内容起始位置
                int length = r.readInt();//文件内容长度
                wxapkgItems.add(new WxapkgItem(name, start, length));
            }

            for (WxapkgItem wxapkgItem : wxapkgItems) {
                File outFile = new File(outDir, wxapkgItem.getName());
                System.out.println(wxapkgItem);
                r.seek(wxapkgItem.getStart());
                byte[] buf = new byte[wxapkgItem.getLength()];
                r.read(buf, 0, wxapkgItem.getLength());

                write(outFile,buf);
            }
        } finally {
            r.close();
        }
        System.out.println("ok");
    }

    private static void write(File outFile,byte[] buf) throws IOException {
        if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
            throw new RuntimeException("无法创建文件夹:" + outFile.getParentFile().getAbsolutePath());
        }
        FileOutputStream out = new FileOutputStream(outFile);
        try{
            out.write(buf);
        }finally {
            out.close();
        }
    }

}
