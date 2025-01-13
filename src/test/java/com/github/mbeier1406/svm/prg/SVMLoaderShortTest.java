package com.github.mbeier1406.svm.prg;

import static com.github.mbeier1406.svm.impl.RuntimeShort.WORTLAENGE_IN_BYTES;
import static com.github.mbeier1406.svm.instructions.InstructionFactory.INSTRUCTIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mbeier1406.svm.MEM.Instruction;
import com.github.mbeier1406.svm.SVMException;
import com.github.mbeier1406.svm.impl.MEMShort;
import com.github.mbeier1406.svm.instructions.InstructionDefinition;
import com.github.mbeier1406.svm.instructions.InstructionInterface;
import com.github.mbeier1406.svm.instructions.InstructionReaderShort;
import com.github.mbeier1406.svm.instructions.Int;
import com.github.mbeier1406.svm.instructions.Mov;
import com.github.mbeier1406.svm.instructions.Nop;
import com.github.mbeier1406.svm.prg.SVMProgram.Data;
import com.github.mbeier1406.svm.prg.SVMProgram.Label;
import com.github.mbeier1406.svm.prg.SVMProgram.LabelType;
import com.github.mbeier1406.svm.prg.SVMProgram.VirtualInstruction;

/**
 * Tests für die Klasse {@linkplain SVMLoaderShort}.
 */
public class SVMLoaderShortTest {

	public static final Logger LOGGER = LogManager.getLogger(SVMLoaderShortTest.class);

	/** Instruktionen @lin {@linkplain Nop}, {@linkplain Int} usw. */
	public static final InstructionInterface<Short> NOP = INSTRUCTIONS.get(Nop.CODE);
	public static final InstructionInterface<Short> INT = INSTRUCTIONS.get(Int.CODE);
	public static final InstructionInterface<Short> MOV = INSTRUCTIONS.get(Mov.CODE);

	/** Verschiedene Label-Definitionen */
	public static final Label LABEL1 = new Label(LabelType.DATA, "text1");
	public static final Label LABEL2 = new Label(LabelType.DATA, "text2");

	/** Zugehörige Label-Referenz */
	public static final Optional<Label> EMPTY_VIRT_LABEL = Optional.empty();
	public static final Optional<Label> LABEL2_VIRT_LABEL = Optional.of(LABEL2);

	/** Labellisten für virtuelle Instruktionen */
	@SuppressWarnings({ "unchecked", "serial" })
	public static final Optional<Label>[] ZERO_REF_LABEL = (Optional<Label>[]) new ArrayList<Optional<Label>>() {}.toArray(new Optional[0]);
	@SuppressWarnings({ "unchecked", "serial" })
	public static final Optional<Label>[] ONE_EMPTY_REF_LABEL = (Optional<Label>[]) new ArrayList<Optional<Label>>() {{ // ein Parameter ohne Label
		add(EMPTY_VIRT_LABEL);
	}}.toArray(new Optional[0]);
	@SuppressWarnings({ "unchecked", "serial" })
	public static final Optional<Label>[] FIVE_EMPTY_REF_LABEL = (Optional<Label>[]) new ArrayList<Optional<Label>>() {{ // fünf Parameter ohne Label
		add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL);
	}}.toArray(new Optional[0]);
	@SuppressWarnings({ "unchecked", "serial" })
	public static final Optional<Label>[] FIVE_REF_LABEL_TEXT1 = (Optional<Label>[]) new ArrayList<Optional<Label>>() {{ // fünf Parameter Label2 an Pos(1)
		add(EMPTY_VIRT_LABEL); add(LABEL2_VIRT_LABEL); add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL); add(EMPTY_VIRT_LABEL);
	}}.toArray(new Optional[0]);

	/** Daten: 4-Worte String */
	public static final Data<Short> FOUR_WORDS_DATA = new Data<Short>(LABEL1, new Short[] { (short) 'a', (short) 'b', (short) 'c', (short) '\n' });

	/** Daten: 3-Worte String */
	public static final Data<Short> THREE_WORDS_DATA = new Data<Short>(LABEL2, new Short[] { (short) 'X', (short) 'Y', (short) '\n' });

	/** Verschiedene Instruktionsdefinition */
	public static final InstructionDefinition<Short> NOP0 = new InstructionDefinition<Short>(NOP, new byte[] {}, Optional.empty()); // NOP
	public static final InstructionDefinition<Short> INT1 = new InstructionDefinition<Short>(INT, new byte[] {1}, Optional.empty()); // INT(1)
	public static final InstructionDefinition<Short> MOV2_REG0 = new InstructionDefinition<Short>(MOV, new byte[] {2,0,2,0,0}, Optional.empty()); // MOV $2 -> REG(0)
	public static final InstructionDefinition<Short> MOV1_REG1 = new InstructionDefinition<Short>(MOV, new byte[] {2,0,1,0,1}, Optional.empty()); // MOV $1 -> REG(1)
	public static final InstructionDefinition<Short> MOVX_REG2 = new InstructionDefinition<Short>(MOV, new byte[] {2,0,0,0,2}, Optional.empty()); // MOV X -> REG(2) X == virt Addr
	public static final InstructionDefinition<Short> MOVL_REG3 = new InstructionDefinition<Short>(MOV, new byte[] {2,0,(byte) THREE_WORDS_DATA.dataList().length,0,3}, Optional.empty()); // MOV len -> REG(3)
	public static final InstructionDefinition<Short> MOV1_REG0 = new InstructionDefinition<Short>(MOV, new byte[] {2,0,1,0,0}, Optional.empty()); // MOV $1 -> REG(0)

	/** Verschiedene virtuelle Instruktion */
	public static final VirtualInstruction<Short> NOP0_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), NOP0, ZERO_REF_LABEL); // INT(1) kein Label
	public static final VirtualInstruction<Short> INT1_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), INT1, ONE_EMPTY_REF_LABEL); // INT(1) kein Label
	public static final VirtualInstruction<Short> MOV2_REG0_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), MOV2_REG0, FIVE_EMPTY_REF_LABEL); // MOV $2 REG(0) kein Label
	public static final VirtualInstruction<Short> MOV1_REG1_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), MOV1_REG1, FIVE_EMPTY_REF_LABEL); // MOV $1 REG(1) kein Label
	public static final VirtualInstruction<Short> MOVX_REG2_LABEL2 = new VirtualInstruction<Short>(Optional.empty(), MOVX_REG2, FIVE_REF_LABEL_TEXT1); // MOV $1 REG(1) kein Label
	public static final VirtualInstruction<Short> MOVL_REG3_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), MOVL_REG3, FIVE_EMPTY_REF_LABEL); // MOV $1 REG(1) kein Label
	public static final VirtualInstruction<Short> MOV1_REG0_OHNE_LABEL = new VirtualInstruction<Short>(Optional.empty(), MOV1_REG0, FIVE_EMPTY_REF_LABEL); // MOV $2 REG(0) kein Label

	/** Das einzuspielende Programm */
	public SVMProgram<Short> svmProgramm;

	/** Der Speicher, in den das {@linkplain #svmProgramm} geladen wird */
	public MEMShort mem;

	/** Zugriff auf einzelne Speicheradressen in {@linkplain #mem} */
	public Instruction<Short> memInstrInterface;

	/** Das zu testende Objekt */
	public SVMLoader<Short> svmLoader;


	/** Initialisiert {@linkplain #svmLoader}, {@linkplain #mem} und {@linkplain #memInstrInterface} */
	@BeforeEach
	public void init() {
		svmLoader = new SVMLoaderShort();
		this.mem = new MEMShort();
		this.mem.clear();
		memInstrInterface = mem.getInstructionInterface();
	}


	/** Erstellt ein einfaches Programm mit etwas IO usw. */
	public static SVMProgram<Short> getKorrektesProgramm() {
		SVMProgram<Short> prg = new SVMProgramShort();

		/* Ein paar Daten einspielen */
		prg.addData(FOUR_WORDS_DATA);
		prg.addData(THREE_WORDS_DATA);

		/* ein paar Instruktionen einspielen */
		prg.addInstruction(NOP0_OHNE_LABEL);		// NOP
		prg.addInstruction(MOV2_REG0_OHNE_LABEL);	// MOV $2 REG(0) 			-- Funktion IO
		prg.addInstruction(MOV1_REG1_OHNE_LABEL);	// MOV $1 REG(1)			-- stdout
		prg.addInstruction(MOVX_REG2_LABEL2);		// MOV .text2 REG(1)		-- Startadresse
		prg.addInstruction(MOVL_REG3_OHNE_LABEL);	// MOV len(.text2) REG(2)	-- Länge Ausgabestring
		prg.addInstruction(INT1_OHNE_LABEL);		// INT(1)					-- Int für Ausgabe
		prg.addInstruction(MOV1_REG0_OHNE_LABEL);	// MOV $1 REG(0) 			-- Funktion EXIT
		prg.addInstruction(MOV1_REG1_OHNE_LABEL);	// MOV $1 REG(1)			-- Exit Code 1
		prg.addInstruction(INT1_OHNE_LABEL);		// INT(1)					-- Int für Exit

		return prg;
	}

	/** Lädt ein korrektes Programm und prüft die Werte im Speicher */
	@Test
	public void testeKorrektesLaden() throws SVMException {

		/* Einige Konstanten für die Längen der Instruktionen */
		var instructionReader = new InstructionReaderShort();
		int nopLenInWords = instructionReader.getInstrLenInWords(NOP, WORTLAENGE_IN_BYTES);
		int intLenInWords = instructionReader.getInstrLenInWords(INT, WORTLAENGE_IN_BYTES);
		int movLenInWords = instructionReader.getInstrLenInWords(MOV, WORTLAENGE_IN_BYTES);
	
		/* Programm laden, darf keine Exception werfen */
		svmProgramm = getKorrektesProgramm();
		svmLoader.load(mem, svmProgramm);

		/* Prüfen, ob die Labelliste korrekt gesetzt ist */
		var labelList = svmLoader.getLabelList();
		LOGGER.info("labelList={}", labelList);
		assertThat(labelList.size(), equalTo(2));
		assertThat(labelList.get(LABEL1), equalTo(mem.getLowAddr()));
		assertThat(labelList.get(LABEL2), equalTo(mem.getLowAddr()+FOUR_WORDS_DATA.dataList().length));

		/* Prüfen, ob die Daten korrekt übernommen wurden */
		int addr = mem.getLowAddr();
		assertThat(memInstrInterface.read(addr+2), equalTo(FOUR_WORDS_DATA.dataList()[2]));
		addr += FOUR_WORDS_DATA.dataList().length;
		assertThat(memInstrInterface.read(addr+1), equalTo(THREE_WORDS_DATA.dataList()[1]));

		/* Prüfen, ob die Instruktionen korrekt übernommen wurden */
		addr = mem.getHighAddr();
		var instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(NOP0.instruction()));
		assertThat(instrDef.params(), equalTo(NOP0.params()));
		addr -= nopLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(MOV2_REG0.instruction()));
		assertThat(instrDef.params(), equalTo(MOV2_REG0.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(MOV1_REG1.instruction()));
		assertThat(instrDef.params(), equalTo(MOV1_REG1.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr); // An dieser Stelel ist die Adresse des Labels text2 in der Parameterliste 
		assertThat(instrDef.instruction(), equalTo(MOVX_REG2.instruction())); // MOVX_REG2.params() bereits eingetragen und wird auch aus dem Speicher gelesen
		assertThat(instrDef.params(), equalTo(MOVX_REG2.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(MOVL_REG3.instruction()));
		assertThat(instrDef.params(), equalTo(MOVL_REG3.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(INT1.instruction()));
		assertThat(instrDef.params(), equalTo(INT1.params()));
		addr -= intLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(MOV1_REG0.instruction()));
		assertThat(instrDef.params(), equalTo(MOV1_REG0.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(MOV1_REG1.instruction()));
		assertThat(instrDef.params(), equalTo(MOV1_REG1.params()));
		addr -= movLenInWords;

		instrDef = instructionReader.getInstruction(mem, addr);
		assertThat(instrDef.instruction(), equalTo(INT1.instruction()));
		assertThat(instrDef.params(), equalTo(INT1.params()));
		addr -= intLenInWords;

	}

}
