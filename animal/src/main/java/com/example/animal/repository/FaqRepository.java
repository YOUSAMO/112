package com.example.animal.repository;

import com.example.animal.entity.Faq;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface FaqRepository {
    List<Faq> getAllFaqs();
    Faq getFaqById(Long faqNo);
    void insertFaq(Faq faq);
    void updateFaq(Faq faq);
    void deleteFaq(Long faqNo);
}

