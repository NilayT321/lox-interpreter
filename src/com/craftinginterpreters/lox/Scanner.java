package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
		private final String source;
		private final List<Token> tokens = new ArrayList<Token>();
		private int start = 0;
		private int current = 0;
		private int line = 1;
		private static final Map<String, TokenType> keywords;

		static {
				keywords = new HashMap<>();
				keywords.put("and",    AND);
				keywords.put("class",  CLASS);
				keywords.put("else",   ELSE);
				keywords.put("false",  FALSE);
				keywords.put("for",    FOR);
				keywords.put("fun",    FUN);
				keywords.put("if",     IF);
				keywords.put("nil",    NIL);
				keywords.put("or",     OR);
				keywords.put("print",  PRINT);
				keywords.put("return", RETURN);
				keywords.put("super",  SUPER);
				keywords.put("this",   THIS);
				keywords.put("true",   TRUE);
				keywords.put("var",    VAR);
				keywords.put("while",  WHILE);
		}

		public Scanner (String source) {
				this.source = source;
		}

		// Get the lexemes 
		public List<Token> scanTokens () {
				while (!isAtEnd()) {
						// beginning of next lexeme
						start = current;
						scanToken();
				}

				tokens.add(new Token(EOF, "", null, line));
				return tokens;
		}

		// Determines if we are at the end of the file 
		private boolean isAtEnd () {
				return current >= source.length();
		}

		// Scan the tokens 
		private void scanToken () {
				char c = advance();
				switch (c) {
						// Single character tokens 
						case '(': addToken(LEFT_PAREN); break;
						case ')': addToken(RIGHT_PAREN); break;
						case '{': addToken(LEFT_BRACE); break;
						case '}': addToken(RIGHT_BRACE); break;
						case ',': addToken(COMMA); break;
						case '.': addToken(DOT); break;
						case '-': addToken(MINUS); break;
						case '+': addToken(PLUS); break;
						case ';': addToken(SEMICOLON); break;
						case '*': addToken(STAR); break;
						
						// Two character lexemes. Certain operators 
						case '!':
								addToken(match('=') ? BANG_EQUAL : BANG);
								break;
						case '=': 
								addToken(match('=') ? EQUAL_EQUAL : EQUAL);
								break;
						case '<':
								addToken(match('=') ? LESS_EQUAL : LESS);
								break;
						case '>':
								addToken(match('=') ? GREATER_EQUAL : GREATER);
								break;
						case '/':
								if (match('/')) {
										// Comment. Ignore the rest of the line 
										while (peek() != '\n' && !isAtEnd()) advance();
								} else {
										addToken(SLASH);
								}
								break;
						
						// Whitespace characters. Ignore them 
						case ' ':
						case '\r':
						case '\t':
								break;

						// Except for newline. Still ignore, but increment the line counter 
						case '\n':
								line++;
								break;
						
						// String literal
						case '"':
								string();
								break;

						default: 
								if (isDigit(c)) {
										// Number literal
										number();
								} else if (isAlpha(c)) {
										// Identifier
										identifier();
								}
								else {
										// Otherwise error
										Lox.error(line, "Unexpected character.");
										break;
								}
				}
		}

		// Match is used to look for two-character lexemes 
		// Only advance if the next character forms a lexeme with the first 
		public boolean match(char expected) {
				if (isAtEnd()) return false;
				if (source.charAt(current) != expected) return false;

				current++;
				return true;
		}

		// Peek at next character, without advancing 
		// Used to handle division vs comments
		private char peek () {
				if (isAtEnd()) return '\0';
				return source.charAt(current);
		}

		// Advance the next character 
		private char advance () {
				return source.charAt(current++);
		}

		// Add a token 
		private void addToken (TokenType type) {
				addToken(type, null);
		}

		private void addToken (TokenType type, Object literal) {
				String text = source.substring(start, current);
				tokens.add(new Token(type, text, literal, line));
		}

		// String is used to hunt for string literals 
		public void string() {
				while (peek() != '"' && !isAtEnd()) {
						if (peek() == '\n') line++;
						advance();
				}

				// Did we reach the end? If yes, then error 
				if (isAtEnd()) {
						Lox.error(line, "Unterminated string.");
						return;
				}

				// The closing " character 
				advance();

				// Trim the surrounding quotes and get the literal 
				String value = source.substring(start + 1, current - 1);
				addToken(STRING, value);
		}

		// Is the current character a number?
		public boolean isDigit (char c) {
				return c >= '0' && c <= '9';
		}

		// Hunt for a number literal. All numbers are stored as floating points 
		public void number () {
				while (isDigit(peek())) advance();

				// Look for a decimal point 
				if (peek() == '.' && isDigit(peekNext())) {
						// Consume the decimal point 
						advance();

						// Get the rest of the number 
						while (isDigit(peek())) advance();
				}

				addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
		}

		public char peekNext () {
				if (current + 1 >= source.length()) return '\0';
				return source.charAt(current + 1);
		}

		// Hunt for an identifier 
		private void identifier () {
				while (isAlphaNumeric(peek())) advance();
				
				String text = source.substring(start, current);
				TokenType type = keywords.get(text);
				if (type == null) type = IDENTIFIER;
				addToken(type);
		}

		// Helpers for identifier() 
		private boolean isAlpha (char c) {
				return (c >= 'a' && c <= 'z') ||
						(c >= 'A' && c <= 'Z') || 
						c == '_';
		}

		private boolean isAlphaNumeric (char c) {
				return isAlpha(c) || isDigit(c);
		}
}
