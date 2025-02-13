package com.sathwick.learning.jpa.practise_2.domain.dto;

import com.sathwick.learning.jpa.inheritance_mapping.joined_table_strategy.domain.Payment;
import com.sathwick.learning.jpa.practise_2.domain.ClinicalData;
import com.sathwick.learning.jpa.practise_2.domain.Patient2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ClinicalDataRequest {
    private Integer patientId;
    private String componentName;
    private String componentValue;

    public ClinicalData toClinicalData(Patient2 patient) {
        return ClinicalData.builder().measuredDateTime(new Timestamp(new Date().getTime())).componentName(this.componentName).componentValue(this.componentValue).patient(patient).build();
    }
}
