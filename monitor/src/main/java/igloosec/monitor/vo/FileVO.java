package igloosec.monitor.vo;

public class FileVO {
    private int fno;
    private int bno;
    private String filename;     //저장할 파일
    private String fileOriName;  //실제 파일
    private String fileurl;

    public int getFno() {
        return fno;
    }

    public void setFno(int fno) {
        this.fno = fno;
    }

    public int getBno() {
        return bno;
    }

    public void setBno(int bno) {
        this.bno = bno;
    }


    public String getFileOriName() {
        return fileOriName;
    }

    public void setFileOriName(String fileOriName) {
        this.fileOriName = fileOriName;
    }



    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }
}
