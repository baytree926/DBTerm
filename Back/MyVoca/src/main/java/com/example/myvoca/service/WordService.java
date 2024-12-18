package com.example.myvoca.service;

import com.example.myvoca.dto.CreateWord;
import com.example.myvoca.dto.EditWord;
import com.example.myvoca.dto.UpdateStats;
import com.example.myvoca.dto.WordDto;
import com.example.myvoca.entity.Vocab;
import com.example.myvoca.entity.Word;
import com.example.myvoca.exception.ApiException;
import com.example.myvoca.repository.VocabRepository;
import com.example.myvoca.repository.WordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.myvoca.code.ApiResponseCode.NO_VOCAB;
import static com.example.myvoca.code.ApiResponseCode.NO_WORD;

@RequiredArgsConstructor
@Slf4j
@Service
public class WordService {
    private final VocabRepository vocabRepository;
    private final WordRepository wordRepository;
    private final StatsService statsService;

    public List<WordDto> getWordByVocabId(Integer vocabId) {
        return wordRepository.findWordsByVocabId(getVocabById(vocabId).getVocabId())
                .stream().map(WordDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CreateWord.Response createWord(Integer vocabId, CreateWord.Request request) {
        Vocab vocab = getVocabById(vocabId);
        Word word = Word.builder()
                .vocab(vocab)
                .expression(request.getExpression())
                .build();
        word = wordRepository.save(word);

        vocab.setWordCount(vocabRepository.countWords(vocab.getVocabId()));

        statsService.updateStats(word.getWordId(), new UpdateStats.Response());

        return CreateWord.Response.fromEntity(word);
    }

    public WordDto getWordDtoById(Integer wordId) {
        return WordDto.fromEntity(getWordById(wordId));
    }

    @Transactional
    public WordDto editWord(Integer wordId, EditWord.Request request) {
        Word word = getWordById(wordId);
        word.setExpression(request.getExpression());

        return WordDto.fromEntity(word);
    }

    @Transactional
    public WordDto deleteWord(Integer wordId) {
        Word word = getWordById(wordId);
        Vocab vocab = getVocabById(word.getVocab().getVocabId());
        wordRepository.delete(word);

        vocab.setWordCount(vocabRepository.countWords(vocab.getVocabId()));
        return WordDto.fromEntity(word);
    }

    private Vocab getVocabById(Integer vocabId) {
        return vocabRepository.findById(vocabId)
                .orElseThrow(() -> new ApiException(NO_VOCAB));
    }

    private Word getWordById(Integer wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> new ApiException(NO_WORD));
    }
}