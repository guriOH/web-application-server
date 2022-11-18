package util;

public class HttpTemplate {

    public static String getTemplate(String title, String slot){
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html");
        sb.append("<html lang=\"ko\">");

        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\">");
        sb.append(" <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        sb.append("<title>게시물 상세 페이지 - " + title + "</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div>");
        sb.append(slot);
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");


        return sb.toString();

    }
}
