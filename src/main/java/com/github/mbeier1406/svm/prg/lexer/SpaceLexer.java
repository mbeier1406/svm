package com.github.mbeier1406.svm.prg.lexer;

import com.github.mbeier1406.svm.prg.lexer.SVMLexer.Symbol;
import com.github.mbeier1406.svm.prg.lexer.SVMLexer.TokenType;
import com.github.mbeier1406.svm.prg.lexer.SVMLexer.TokenTypeLexer;

/**
 * Definiert die Funktion zur lexikalischen Analyse eines {@linkplain TokenType#SPACE}.
 */
public class SpaceLexer {

	/**
	 * Funktion zur Verarbeitung eines Leerzeichens:
	 * <ul>
	 * <li>In die Liste der {@linkplain Symbol}e wird nichts eingefügt</li>
	 * <li>Wenn gerade ein Symbol gelesen wird (lastTokenType ist nicht <b>null</b> bzw. ungleich {@linkplain TokenType#SPACE} handelt es sich um einen Fehler</li>
	 * <li>Es wird wieder {@linkplain TokenType#SPACE} zurückgegeben, da mehrere Leerzeichen hintereinander erlaubt sind</li>
	 * </ul>
	 * Das Leerzeichen trennt {@linkplain TokenGroupLexer Tokengruppen}.
	 */
	@SuppressWarnings("unused")
	public static final TokenTypeLexer TOKEN_SCANNER = (symbolList, tokenValue, lastTokenType) -> {
		if ( lastTokenType != null ) {
			if ( lastTokenType != TokenType.SPACE )
				throw new IllegalArgumentException("Leerezeichen gefunden während folgendes Sysmbol gelesen wurde: "+lastTokenType);
		}
		else {
			// Nichts zu tun
		}
		return TokenType.SPACE; // Damit mehrere Leerzeichen hintereinander gelesen werden können
	};

}
