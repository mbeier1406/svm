package com.github.mbeier1406.svm.instructions;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Optional;

import com.github.mbeier1406.svm.ALU;
import com.github.mbeier1406.svm.MEM;
import com.github.mbeier1406.svm.SVM;
import com.github.mbeier1406.svm.SVMException;
import com.github.mbeier1406.svm.prg.SVMProgram;

/**
 * Neben dem eigentlichen Maschinenbefehl/der Instruktion (definiert in {@linkplain InstructionInterface}
 * gehören auch dessen/deren Parameter (als Array von Bytes) zur vollständigen Darstellung im Speicher.
 * Ebenfalls von Interesse ist, wie viele Speicherwörter die Instruktion mit ihrem/n Parameter/n im
 * konkreten Speicher {@linkplain MEM} verbraucht.
 * <p>
 * Wenn eine Instruktion mit {@linkplain InstructionReaderInterface#getInstruction(com.github.mbeier1406.svm.MEM.Instruction, int)}
 * eingelesen sird, muss einerseits die {@linkplain InstructionInterface Instruktion} selber, die zu übergebenden Parameter
 * (als Liste von Bytes) und die Länge der Instruktion (mit Paramteren) im Speicher in Wortlänge der {@linkplain SVM}
 * geliefert werden (eine Instruktion "weiß" das nicht, da sie die Wortlänge des Speichers nicht kennt).
 * Diese Länge wird benötigt, der der Instructionpointer der {@linkplain ALU} um diesen Betrag verschoben werden muss, um auf die
 * nächste, auszuführende Instruktion zu zeigen.
 * </p>
 * Wenn ein SVM-Programm eingelesen wird, werden seine Instruktionen in {@linkplain SVMProgram.VirtualInstruction} gespeichert.
 * An dieser Stelle ist die Größe der Instruktion im Speicher der konkreten SVM noch nicht bekannt. Deswegen kann das
 * Feld {@linkplain InstructionDefinition#lenInWords} an dieser Stelle noch nicht gesetzt werden. Es handelt sich also um
 * ein Optionales feld
 * </p>
 * @param instruction die Instruktion / der Maschinenbefehl
 * @param params Liste der Parameter zur Instruktion
 * @param lenInWords die gesamte Länge der Instruktion mit Parametern in Wortlänge der SVM soweit bekannt
 * @param <T> Die Wortlänge der {@linkplain SVM}
 * @see InstructionReaderInterface
 * @see SVMProgram
 */
public record InstructionDefinition<T>(InstructionInterface<T> instruction, byte[] params, Optional<Integer> lenInWords) implements Serializable {
	public InstructionDefinition {
		requireNonNull(instruction, "instruction");
		requireNonNull(params, "params");
		requireNonNull(lenInWords, "lenInWords");
		int erwarteteAnzahlParameter = instruction.getAnzahlParameter();
		int erhalteneAnzahlParameter = params.length;
		if ( erwarteteAnzahlParameter != erhalteneAnzahlParameter )
			throw new IllegalArgumentException("instruction="+instruction+": erwartete Parameter: "+erwarteteAnzahlParameter+"; erhalteneAnzahlParameter: "+erhalteneAnzahlParameter);
	}
	/** Liefert die Größe der Instruktion mit Parametern im Hauptspeicher damit der IP entsprechend weitergesetzt werden kann */
	public int getLenInMemoryInWords() throws SVMException {
		return this.lenInWords.orElseThrow(() -> new SVMException("leninWords nicht gesetzt: "+this));
	}
}
