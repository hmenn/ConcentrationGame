package com.hasanmen.concentrationgame.POJO;

/**
 * Created by hmenn on 23.05.2017.
 */

// I used
public class PixabayImage {
    private String previewURL;
    private int imageWidth;
    private int imageHeight;
    private String webformatURL;

    public PixabayImage() {
    }

    public PixabayImage(String previewURL,String webformatURL, int imageWidth, int imageHeight) {
        this.previewURL = previewURL;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.webformatURL=webformatURL;
    }

    public String getWebformatURL() {
        return webformatURL;
    }

    public void setWebformatURL(String webformatURL) {
        this.webformatURL = webformatURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Override
    public String toString() {
        return "PixabayImage{" +
                "previewURL='" + previewURL + '\'' +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                ", webformatURL='" + webformatURL + '\'' +
                '}';
    }
}
