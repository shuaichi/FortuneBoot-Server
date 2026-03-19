package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.factory.fortune.model.FortuneMemberRelationModel;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:32
 **/

@Component
@RequiredArgsConstructor
public class FortuneMemberRelationFactory {
    private final FortuneMemberRelationRepo fortuneMemberRelationRepo;

    public FortuneMemberRelationModel create() {
        return new FortuneMemberRelationModel(fortuneMemberRelationRepo);
    }
}