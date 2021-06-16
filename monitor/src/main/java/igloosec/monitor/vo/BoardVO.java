package igloosec.monitor.vo;


public class BoardVO {

    private int bno;
    private String subject;
    private String content;
    private String writer;
    private String regDate;
    private int idx;
    private String rscparam;
    private String recparam;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }


    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public int getBno() {
        return bno;
    }

    public void setBno(int bno) {
        this.bno = bno;
    }


    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getRscparam() {
        return rscparam;
    }

    public void setRscparam(String rscparam) {
        this.rscparam = rscparam;
    }

    public String getRecparam() {
        return recparam;
    }

    public void setRecparam(String recparam) {
        this.recparam = recparam;
    }
}
