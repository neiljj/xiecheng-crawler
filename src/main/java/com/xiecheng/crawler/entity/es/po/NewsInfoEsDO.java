package com.xiecheng.crawler.entity.es.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.experimental.Accessors;
import org.zxp.esclientrhl.annotation.ESID;
import org.zxp.esclientrhl.annotation.ESMapping;
import org.zxp.esclientrhl.annotation.ESMetaData;
import org.zxp.esclientrhl.enums.Analyzer;
import org.zxp.esclientrhl.enums.DataType;

import java.time.LocalDateTime;

/**
 * 新闻 es do
 * @author nijichang
 * @since 2021-07-26 11:11:18
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
@ESMetaData(indexName = "news-info")
@EqualsAndHashCode
@Accessors(chain = true)
public class NewsInfoEsDO {

    private static final long serialVersionUID = 362256892629374292L;

    @ESID
    @ESMapping(datatype = DataType.long_type)
    private Long id;

    @ESMapping(datatype = DataType.text_type,analyzer = Analyzer.ik_max_word)
    private String title;

    @ESMapping(datatype = DataType.text_type,analyzer = Analyzer.ik_smart)
    private String content;

    @ESMapping(datatype = DataType.keyword_type)
    private String url;

    @ESMapping(datatype = DataType.keyword_type)
    private String keyword;

    @ESMapping(datatype = DataType.keyword_type)
    private String source;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @ESMapping(datatype = DataType.date_type)
    private LocalDateTime time;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @ESMapping(datatype = DataType.date_type)
    private LocalDateTime create_time;
}
