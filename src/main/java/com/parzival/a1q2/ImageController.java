package com.parzival.a1q2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class ImageController {
    @FXML
    private ImageView imageView;

    Stage stage;

    public void openFile(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.bmp"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            System.out.printf("Open image file\n");
            loadImageFile(file);
        }
    }

    //BMP file formatting reference:
    // https://docs.fileformat.com/image/bmp/
    // https://en.wikipedia.org/wiki/BMP_file_format
    private void loadImageFile(File file) throws IOException {
        try(FileInputStream fis = new FileInputStream(file)){
            DataInputStream dis = new DataInputStream(fis);

            //Exception handling(ensure the file is BMP)
            int fileType = readShortLittleEndian(dis); //2 bytes in File Header
            System.out.println(fileType);

            //string 'BM' in ASCII is 0x424D
            //However, my helper function readShortLittleEndian() put the high byte in the front,
            // so I compare this int fileType with 0x4D42
            if (fileType != 0x4D42){
                throw new IOException("Not a BMP file");
            }
            
            //skip the rest bytes in the File Header
            dis.skipBytes(12);

            //read the DIB Header
            int size = readIntLittleEndian(dis);
            int width = readIntLittleEndian(dis);
            int height = readIntLittleEndian(dis);
            dis.skipBytes(2); //skip colour plane
            int bitDepth = readShortLittleEndian(dis);
            if(bitDepth != 24){
                throw new IOException("Unsupported bit depth" + bitDepth);
            }
            dis.skipBytes(24); //skip the rest bytes in DIB header

            WritableImage image = new WritableImage(width,height);
            PixelWriter pw = image.getPixelWriter();

            int padding = (4 - (width * 3) % 4) % 4;  // 每行需要的填充字节数
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int blue = dis.readUnsignedByte();
                    int green = dis.readUnsignedByte();
                    int red = dis.readUnsignedByte();
                    Color color = Color.rgb(red, green, blue);
                    pw.setColor(x, y, color);
                }
                dis.skipBytes(padding);  // 跳过填充字节
            }

            imageView.setImage(image);
        }

    }

    //helper method to read the 2 bytes
    private static int readShortLittleEndian(DataInputStream dis) throws IOException {
        return dis.readUnsignedByte() | (dis.readUnsignedByte() << 8);
    }

    //helper method to read an int(4 bytes) size, width, height in DIB Header
    private static int readIntLittleEndian(DataInputStream dis) throws IOException {
        return dis.readUnsignedByte() | dis.readUnsignedByte() << 8 | dis.readUnsignedByte() << 16 |
                dis.readUnsignedByte() << 24;
    }

}