package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
		static boolean hadError = false;

		public static void main (String[] args) throws IOException {
				
				if (args.length > 1) {
						System.out.println("Usage: jlox [script]");
						System.exit(64);
				} else if (args.length == 1) {
						runFile(args[0]);
				} else {
						runPrompt();
				}

		}

		// Mode 1: run a file 
		private static void runFile (String path) throws IOException {
				byte[] bytes = Files.readAllBytes(Paths.get(path));
				run(new String(bytes, Charset.defaultCharset()));
				if (hadError) System.exit(65);
		}

		// Mode 2: run the interactive REPL
		private static void runPrompt () throws IOException {
				InputStreamReader input = new InputStreamReader(System.in);
				BufferedReader reader = new BufferedReader(input);

				for (;;) {
						System.out.print("lox> ");
						String line = reader.readLine();
						if (line == null) break;
						run(line);
						hadError = false;
				}
		}

		// Run function used in both files above
		private static void run (String source) throws IOException {
				Scanner scanner = new Scanner(source);
				List<Token> tokens = scanner.scanTokens();

				// Placeholder, just print tokens 
				for (Token token : tokens) {
						System.out.println(token);
				}
		}

		// Things for error reporting 
		public static void error (int line, String message) {
				report(line, "", message);
		}

		public static void report (int line, String where, String message) {
				System.err.println("[line " + line + "] error" + where + ": " + message);
				hadError = true;
		}


}

