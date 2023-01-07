package com.newyear.mainproject.plan.serivce;

import com.newyear.mainproject.exception.BusinessLogicException;
import com.newyear.mainproject.exception.ExceptionCode;
import com.newyear.mainproject.plan.entity.Plan;
import com.newyear.mainproject.plan.entity.PlanDate;
import com.newyear.mainproject.plan.repository.PlanDateRepository;
import com.newyear.mainproject.plan.repository.PlanRepository;
import com.newyear.mainproject.util.DateCalculation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlanService {
   private final PlanRepository planRepository;
   private final PlanDateRepository planDateRepository;

    public PlanService(PlanRepository planRepository, PlanDateRepository planDateRepository) {
        this.planRepository = planRepository;
        this.planDateRepository = planDateRepository;
    }

    /**
     * 일정 등록
     */
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    /**
     * 일정 수정
     */
    public Plan updatePlan(Plan plan) {
        Plan findPlan = findVerifiedPlan(plan.getPlanId());

        //일정 수정하면서 동시에 같은 plan_id 값을 가진 plan_date 의 값들이 수정 - 삭제 후 다시 생성
        planDateRepository.deleteAllByPlan(plan);

        Optional.ofNullable(plan.getStartDate())
                .ifPresent(startDate -> findPlan.setStartDate(startDate));
        Optional.ofNullable(plan.getEndDate())
                .ifPresent(endDate -> findPlan.setEndDate(endDate));

        return planRepository.save(plan);
    }

    /**
     * 일정-날짜 등록
     */
    public void createPlanDate(Plan plan) {
        List<String> dateList = DateCalculation.dateCal(plan.getStartDate(), plan.getEndDate());
        System.out.println(dateList.size());
        //일정 등록하면서 동시에 plan_date 테이블에 값 저장

        for(String date : dateList) {
            PlanDate planDate = new PlanDate();
            planDate.setPlanDate(date); // 시작일정-끝일정 사이의 일정들을 전부 등록
            planDate.setPlan(plan);
            planDateRepository.save(planDate);
        }
    }


    /**
     * 일정 삭제
     */
    public void deletePlan(Long planId) {
        Plan findPlan = findVerifiedPlan(planId);
        planRepository.delete(findPlan);
    }


    /**
     * 일정 존재 여부 확인
     */
    private Plan findVerifiedPlan(Long planId) {
        Optional<Plan> optionalPlan = planRepository.findById(planId);
        return optionalPlan.orElseThrow(() -> new BusinessLogicException(ExceptionCode.PLAN_NOT_FOUND));
    }
}