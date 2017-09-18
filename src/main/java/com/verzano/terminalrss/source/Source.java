package com.verzano.terminalrss.source;

import com.google.gson.annotations.SerializedName;
import com.verzano.terminalrss.content.ContentType;
import com.verzano.terminalrss.tui.TuiStringable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.verzano.terminalrss.content.ContentType.NULL_CONTENT_TYPE;

@Getter
@AllArgsConstructor
public class Source implements TuiStringable {
  public static final long NULL_SOURCE_ID = -1L;
  public static final Source NULL_SOURCE = new Source(NULL_SOURCE_ID, "", NULL_CONTENT_TYPE, "", new Date(0), "");

  @SerializedName("id")
  private final long id;

  @SerializedName("uri")
  private final String uri;

  @Setter
  @SerializedName("content_type")
  private ContentType contentType;

  @Setter
  @SerializedName("content_tag")
  private String contentTag;

  @Setter
  @SerializedName("published_date")
  private Date publishedDate;

  @Setter
  @SerializedName("title")
  private String title;

  @Override
  public String toTuiString() {
    return title;
  }
}
