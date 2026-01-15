package be.controller;

import be.domain.*;
import be.service.*;
import be.util.api.*;
import java.util.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final CategoryRecommender categoryRecommender;

    @GetMapping("/category")
    public ApiResponse<List<Category>> getRecommend()   {
        return ApiResponse.success(categoryRecommender.recommendCategory());
    }
}
