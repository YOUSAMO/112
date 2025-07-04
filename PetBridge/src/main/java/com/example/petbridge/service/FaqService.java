package com.example.petbridge.service;


import com.example.petbridge.entity.Faq;
import com.example.petbridge.repository.FaqRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<Faq> getAllFaqs() {
        return faqRepository.getAllFaqs();
    }

    public Faq getFaqById(Long faqNo) {
        return faqRepository.getFaqById(faqNo);
    }

    public void createFaq(Faq faq) {
        faqRepository.insertFaq(faq);
    }

    public void updateFaq(Faq faq) {
        faqRepository.updateFaq(faq);
    }

    public void deleteFaq(Long faqNo) {
        faqRepository.deleteFaq(faqNo);
    }
}