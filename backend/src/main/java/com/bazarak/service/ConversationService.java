package com.bazarak.service;

import com.bazarak.entity.Advertisement;
import com.bazarak.entity.Conversation;
import com.bazarak.entity.Message;
import com.bazarak.entity.User;
import com.bazarak.exception.conversation.*;
import com.bazarak.repository.ConversationRepository;
import com.bazarak.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationService {
}
