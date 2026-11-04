package com.example.sds;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.Map;

@RestController
public class IdempotencyController {
    private final StringRedisTemplate redis;
    public IdempotencyController(StringRedisTemplate redis) { this.redis = redis; }

    @PostMapping("/payments")
    ResponseEntity<?> pay(@RequestHeader("Idempotency-Key") String key, @RequestBody String body) {
        String bodyHash = Integer.toHexString(body.hashCode());
        // SET NX: only the first concurrent request wins
        boolean first = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent("idem:" + key, "INPROGRESS|" + bodyHash, Duration.ofHours(24)));
        if (!first) {
            String stored = redis.opsForValue().get("idem:" + key);
            if (stored != null && !stored.endsWith("|" + bodyHash) && !stored.startsWith("DONE"))
                return ResponseEntity.unprocessableEntity().body(Map.of("error", "KEY_REUSED_WITH_OTHER_BODY"));
            return ResponseEntity.status(201).header("Idempotent-Replayed", "true").body(Map.of("paymentId", "p-9"));
        }
        redis.opsForValue().set("idem:" + key, "DONE|" + bodyHash, Duration.ofHours(24));
        return ResponseEntity.status(201).body(Map.of("paymentId", "p-9"));
    }
}
