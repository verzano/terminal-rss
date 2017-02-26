package com.verzano.terminalrss.source;

import com.google.gson.annotations.SerializedName;
import com.verzano.terminalrss.content.ContentType;
import com.verzano.terminalrss.ui.TUIStringable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.verzano.terminalrss.content.ContentType.NULL_TYPE;

@Getter
@AllArgsConstructor
public class Source implements TUIStringable {
  public static final Source NULL_SOURCE = new Source(-1L, "", NULL_TYPE, "", new Date(0), "");

  @SerializedName("id")
  private final long id;

  @SerializedName("uri")
  private final String uri;

  @Setter
  @SerializedName("content_type")
  private final ContentType contentType;

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
  public String toTUIString() {
    return title;
  }
}
