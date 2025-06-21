package com.psy.dear.domain.use_case.services

import com.psy.dear.data.static.DassTestData
import com.psy.dear.data.static.MbtiTestData
import com.psy.dear.domain.model.DassQuestion
import com.psy.dear.domain.model.MbtiQuestion
import com.psy.dear.domain.model.TestOption
import javax.inject.Inject

class GetDassTestUseCase @Inject constructor() {
    fun getQuestions(): List<DassQuestion> = DassTestData.questions
    fun getOptions(): List<TestOption> = DassTestData.options
}

class GetMbtiTestUseCase @Inject constructor() {
    fun getQuestions(): List<MbtiQuestion> = MbtiTestData.questions
    fun getOptions(): List<TestOption> = MbtiTestData.options
}
