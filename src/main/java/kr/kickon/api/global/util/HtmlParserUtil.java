package kr.kickon.api.global.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class HtmlParserUtil {
  private static final Pattern YOUTUBE_EMBED_PATTERN =
      Pattern.compile("src\\s*=\\s*\"(https://www\\.youtube\\.com/embed/([^\"]+))\"");

  /**
   * HTML 콘텐츠에서 유튜브 embed 링크를 추출하여 watch 링크로 변환
   */
  public static String[] extractYoutubeWatchLinks(String content) {
    Matcher matcher = YOUTUBE_EMBED_PATTERN.matcher(content);
    List<String> watchLinks = new ArrayList<>();

    while (matcher.find()) {
      String videoId = matcher.group(2); // (https://...embed/)(videoId)
      watchLinks.add("https://www.youtube.com/watch?v=" + videoId);
    }

    return watchLinks.toArray(new String[0]);
  }
}
