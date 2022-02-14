package io.github.engineeringov.website.api.model.api.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaSiteVerifyResponse {
    @JsonProperty("success")
    Boolean success;
    @JsonFormat(pattern="uuuu-MM-dd[ ]['T']HH:mm:ss[X]") //2022-01-05T08:50:41Z
    @JsonProperty("challenge_ts")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    LocalTime challengeTs;
    @JsonProperty("hostname")
    String hostname;
    @JsonProperty("action")
    String action;
    @JsonProperty("score")
    float score;
    @JsonProperty("error-codes")
    String[] errorCodes;
}
