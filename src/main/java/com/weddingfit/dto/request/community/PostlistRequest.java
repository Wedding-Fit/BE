package com.weddingfit.dto.request.community;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostlistRequest {

    @NotNull
    private Category category;

    public enum Category {
        WEDDING, SAVING, HOUSING, TAX, USED, TIPS, ETC;

        public static Category from(String value) {
            if (value == null) return null;
            return Category.valueOf(value.trim().toUpperCase());
        }
    }
}
