package app.tuxguitar.io.tg.v10;

import java.io.DataInputStream;
import java.io.IOException;

import app.tuxguitar.gm.GMChannelRoute;
import app.tuxguitar.io.base.TGFileFormat;
import app.tuxguitar.io.base.TGFileFormatException;
import app.tuxguitar.io.base.TGSongReader;
import app.tuxguitar.io.base.TGSongReaderHandle;
import app.tuxguitar.io.tg.v15.TGFileFormatDetectorImpl;
import app.tuxguitar.io.tg.TGFileFormatVersion;
import app.tuxguitar.song.factory.TGFactory;
import app.tuxguitar.song.managers.TGSongManager;
import app.tuxguitar.song.models.TGBeat;
import app.tuxguitar.song.models.TGChannel;
import app.tuxguitar.song.models.TGChannelParameter;
import app.tuxguitar.song.models.TGChord;
import app.tuxguitar.song.models.TGColor;
import app.tuxguitar.song.models.TGDivisionType;
import app.tuxguitar.song.models.TGDuration;
import app.tuxguitar.song.models.TGLyric;
import app.tuxguitar.song.models.TGMarker;
import app.tuxguitar.song.models.TGMeasure;
import app.tuxguitar.song.models.TGMeasureHeader;
import app.tuxguitar.song.models.TGNote;
import app.tuxguitar.song.models.TGNoteEffect;
import app.tuxguitar.song.models.TGSong;
import app.tuxguitar.song.models.TGString;
import app.tuxguitar.song.models.TGTempo;
import app.tuxguitar.song.models.TGText;
import app.tuxguitar.song.models.TGTimeSignature;
import app.tuxguitar.song.models.TGTrack;
import app.tuxguitar.song.models.TGVoice;
import app.tuxguitar.song.models.effects.TGEffectBend;
import app.tuxguitar.song.models.effects.TGEffectGrace;
import app.tuxguitar.song.models.effects.TGEffectHarmonic;
import app.tuxguitar.song.models.effects.TGEffectTremoloBar;
import app.tuxguitar.song.models.effects.TGEffectTremoloPicking;
import app.tuxguitar.song.models.effects.TGEffectTrill;

public class TGSongReaderImpl extends TGStream implements TGSongReader{

	public static final TGFileFormatVersion SUPPORTED_FORMAT = new TGFileFormatVersion(TG_FORMAT, TG_FORMAT_VERSION);

	private DataInputStream dataInputStream;
	private TGFactory factory;

	public TGSongReaderImpl() {
		super();
	}

	public void read(TGSongReaderHandle handle) throws TGFileFormatException {
		try {
			this.factory = handle.getFactory();
			this.dataInputStream = new DataInputStream(handle.getInputStream());

			TGFileFormat fileFormat = new TGFileFormatDetectorImpl(SUPPORTED_FORMAT).getFileFormat(this.dataInputStream);
			if( fileFormat == null || !fileFormat.equals(this.getFileFormat()) ) {
				throw new TGFileFormatException(TGFileFormatException.UNSUPPORTED_VERSION);
			}

			TGSong song = this.read();
			this.dataInputStream.close();
			handle.setSong(song);
		} catch (Throwable throwable) {
			throw new TGFileFormatException(throwable);
		}
	}

	private TGSong read(){
		TGSong song = this.factory.newSong();

		//leo el nombre
		song.setName(readUnsignedByteString());

		//leo el artista
		song.setArtist(readUnsignedByteString());

		//leo el album
		song.setAlbum(readUnsignedByteString());

		//leo el autor
		song.setAuthor(readUnsignedByteString());

		//leo la cantidad de measure headers
		int headerCount = readShort();

		//leo las pistas
		TGMeasureHeader lastHeader = null;
		long headerStart = TGDuration.QUARTER_TIME;
		for(int i = 0;i < headerCount;i++){
			TGMeasureHeader header = readMeasureHeader(i + 1,headerStart,lastHeader);
			song.addMeasureHeader(header);
			headerStart += header.getLength();
			lastHeader = header;
		}

		//leo la cantidad de pistas
		int trackCount = readByte();

		//leo las pistas
		for(int i = 0;i < trackCount;i++){
			song.addTrack(readTrack(i + 1,song));
		}

		return song;
	}

	private TGTrack readTrack(int number,TGSong song){
		//header
		int header = readHeader();

		TGTrack track = this.factory.newTrack();

		track.setNumber(number);

		//leo el nombre
		track.setName(readUnsignedByteString());

		//leo el canal
		readChannel(song, track);

		//leo la cantidad de compases
		int measureCount = song.countMeasureHeaders();

		//leo los compases
		TGMeasure lastMeasure = null;
		for(int i = 0;i < measureCount;i++){
			TGMeasure measure = readMeasure(song.getMeasureHeader(i),lastMeasure);
			track.addMeasure(measure);
			lastMeasure = measure;
		}

		//leo la cantidad de cuerdas
		int stringCount = readByte();

		//leo las cuerdas
		for(int i = 0;i < stringCount;i++){
			track.getStrings().add(readInstrumentString(i + 1));
		}

		//leo el offset
		track.setOffset(TGTrack.MIN_OFFSET + readByte());

		//leo el color
		readRGBColor(track.getColor());

		//leo el lyrics
		if(((header & TRACK_LYRICS) != 0)){
			readLyrics(track.getLyrics());
		}

		return track;
	}

	private TGMeasureHeader readMeasureHeader(int number,long start,TGMeasureHeader lastMeasureHeader){
		int header = readHeader();

		TGMeasureHeader measureHeader = this.factory.newHeader();
		measureHeader.setNumber(number);
		measureHeader.setStart(start);

		//leo el time signature
		if(((header & MEASURE_HEADER_TIMESIGNATURE) != 0)){
			readTimeSignature(measureHeader.getTimeSignature());
		}else if(lastMeasureHeader != null){
			measureHeader.getTimeSignature().copyFrom(lastMeasureHeader.getTimeSignature());
		}

		//leo el tempo
		if(((header & MEASURE_HEADER_TEMPO) != 0)){
			readTempo(measureHeader.getTempo());
		}else if(lastMeasureHeader != null){
			measureHeader.getTempo().copyFrom(lastMeasureHeader.getTempo());
		}

		//leo el comienzo de la repeticion
		measureHeader.setRepeatOpen((header & MEASURE_HEADER_REPEAT_OPEN) != 0);

		//leo el numero de repeticiones
		if(((header & MEASURE_HEADER_REPEAT_CLOSE) != 0)){
			 measureHeader.setRepeatClose(readShort());
		}

		//leo los finales alternativos
		if(((header & MEASURE_HEADER_REPEAT_ALTERNATIVE) != 0)){
			 measureHeader.setRepeatAlternative(readByte());
		}

		//leo el marker
		if(((header & MEASURE_HEADER_MARKER) != 0)){
			measureHeader.setMarker(readMarker(number));
		}

		measureHeader.setTripletFeel((lastMeasureHeader != null)?lastMeasureHeader.getTripletFeel():TGMeasureHeader.TRIPLET_FEEL_NONE);
		if(((header & MEASURE_HEADER_TRIPLET_FEEL) != 0)){
			measureHeader.setTripletFeel(readByte());
		}

		return measureHeader;
	}

	private TGMeasure readMeasure(TGMeasureHeader measureHeader,TGMeasure lastMeasure){
		int header = readHeader();

		TGMeasure measure = this.factory.newMeasure(measureHeader);
		TGBeatData data = new TGBeatData(measure);

		//leo la los beats
		readBeats(measure, data);

		//leo la clave
		measure.setClef( (lastMeasure == null)?TGMeasure.CLEF_TREBLE:lastMeasure.getClef());
		if(((header & MEASURE_CLEF) != 0)){
			measure.setClef(readByte());
		}

		//leo el key signature
		measure.setKeySignature((lastMeasure == null)?0:lastMeasure.getKeySignature());
		if(((header & MEASURE_KEYSIGNATURE) != 0)){
			measure.setKeySignature(readByte());
		}

		return measure;
	}

	private void readChannel(TGSong song, TGTrack track){
		int header = readHeader();

		TGChannel channel = this.factory.newChannel();
		TGChannelParameter gmChannel1Param = this.factory.newChannelParameter();
		TGChannelParameter gmChannel2Param = this.factory.newChannelParameter();

		//leo el canal
		int channel1 = (readByte() & 0xff);
		gmChannel1Param.setKey(GMChannelRoute.PARAMETER_GM_CHANNEL_1);
		gmChannel1Param.setValue(Integer.toString(channel1));

		//leo el canal de efectos
		int channel2 = (readByte() & 0xff);
		gmChannel2Param.setKey(GMChannelRoute.PARAMETER_GM_CHANNEL_2);
		gmChannel2Param.setValue(Integer.toString(channel2));

		// Parseo el banco de sonidos
		channel.setBank( channel1 == 9 ? TGChannel.DEFAULT_PERCUSSION_BANK : TGChannel.DEFAULT_BANK);

		//leo el instrumento
		channel.setProgram(readByte());

		//leo el volumen
		channel.setVolume(readByte());

		//leo el balance
		channel.setBalance(readByte());

		//leo el chorus
		channel.setChorus(readByte());

		//leo el reverb
		channel.setReverb(readByte());

		//leo el phaser
		channel.setPhaser(readByte());

		//leo el tremolo
		channel.setTremolo(readByte());

		//leo el solo
		track.setSolo((header & CHANNEL_SOLO) != 0);

		//leo el mute
		track.setMute((header & CHANNEL_MUTE) != 0);

		//------------------------------------------//
		for( int i = 0 ; i < song.countChannels() ; i ++ ){
			TGChannel channelAux = song.getChannel(i);
			for( int n = 0 ; n < channelAux.countParameters() ; n ++ ){
				TGChannelParameter channelParameter = channelAux.getParameter( n );
				if( channelParameter.getKey().equals(GMChannelRoute.PARAMETER_GM_CHANNEL_1) ){
					if( Integer.toString(channel1).equals(channelParameter.getValue()) ){
						channel.setChannelId(channelAux.getChannelId());
					}
				}
			}
		}
		if( channel.getChannelId() <= 0 ){
			channel.setChannelId( song.countChannels() + 1 );
			channel.setName(new TGSongManager(this.factory).createChannelNameFromProgram(song, channel));
			channel.addParameter(gmChannel1Param);
			channel.addParameter(gmChannel2Param);
			song.addChannel(channel);
		}
		track.setChannelId(channel.getChannelId());
	}

	private void readBeats(TGMeasure measure,TGBeatData data){
		int header = BEAT_HAS_NEXT;
		while(((header & BEAT_HAS_NEXT) != 0)){
			header = readHeader();
			readBeat(header, measure, data);
		}
	}

	private void readBeat(int header, TGMeasure measure,TGBeatData data){
		TGBeat beat = this.factory.newBeat();
		TGVoice voice = beat.getVoice(0);

		beat.setStart(data.getStart());
		voice.setEmpty( false );

		//leo la duracion
		if(((header & BEAT_NEXT_DURATION) != 0)){
			readDuration(data.getDuration());
		}

		//leo las notas
		if(((header & BEAT_HAS_NOTES) != 0)){
			readNotes(beat, data);
		}

		//leo el acorde
		if(((header & BEAT_HAS_CHORD) != 0)){
			readChord(beat);
		}

		//leo el texto
		if(((header & BEAT_HAS_TEXT) != 0)){
			readText(beat);
		}

		voice.getDuration().copyFrom(data.getDuration());

		measure.addBeat(beat);

		data.setStart(data.getStart() + data.getDuration().getTime());
	}

	private void readNotes(TGBeat beat,TGBeatData data){
		int header = NOTE_HAS_NEXT;
		while(((header & NOTE_HAS_NEXT) != 0)){
			header = readHeader();
			readNote(header, beat, data);
		}
	}

	private void readNote(int header,TGBeat beat,TGBeatData data){
		TGNote note = this.factory.newNote();
		TGVoice voice = beat.getVoice(0);

		//leo el valor
		note.setValue(readByte());

		//leo la cuerda
		note.setString(readByte());

		//leo la ligadura
		note.setTiedNote((header & NOTE_TIED) != 0);

		//leo el velocity
		if(((header & NOTE_VELOCITY) != 0)){
			data.setVelocity(readByte());
		}
		note.setVelocity(data.getVelocity());

		//leo los efectos
		if(((header & NOTE_EFFECT) != 0)){
			readNoteEffect(note.getEffect());
		}

		voice.addNote(note);
	}

	private void readChord(TGBeat beat){
		TGChord chord = this.factory.newChord(readByte());

		//leo el nombre
		chord.setName( readUnsignedByteString() );

		//leo el primer fret
		chord.setFirstFret(readByte());

		//leo las cuerdas
		for(int string = 0; string < chord.countStrings(); string ++){
			chord.addFretValue(string, readByte());
		}
		beat.setChord(chord);
	}

	private void readText(TGBeat beat){
		TGText text = this.factory.newText();

		//leo el texto
		text.setValue(readUnsignedByteString());

		beat.setText(text);
	}

	private TGString readInstrumentString(int number){
		TGString string = this.factory.newString();

		string.setNumber(number);

		//leo el valor
		string.setValue(readByte());

		return string;
	}

	private void readTempo(TGTempo tempo){
		//leo el valor
		tempo.setQuarterValue(readShort());
	}

	private void readTimeSignature(TGTimeSignature timeSignature){
		//leo el numerador
		timeSignature.setNumerator(readByte());

		//leo el denominador
		readDuration(timeSignature.getDenominator());
	}

	private void readDuration(TGDuration duration){
		int header = readHeader();

		// leo el puntillo
		duration.setDotted((header & DURATION_DOTTED) != 0);

		//leo el doble puntillo
		duration.setDoubleDotted((header & DURATION_DOUBLE_DOTTED) != 0);

		//leo el valor
		duration.setValue(readByte());

		//leo el tipo de divisiones
		if(((header & DURATION_NO_TUPLET) != 0)){
			readDivisionType(duration.getDivision());
		}
		else{
			duration.getDivision().copyFrom(TGDivisionType.NORMAL);
		}
	}

	private void readDivisionType(TGDivisionType divisionType){
		//leo los enters
		divisionType.setEnters(readByte());

		//leo los tiempos
		divisionType.setTimes(readByte());
	}

	private void readNoteEffect(TGNoteEffect effect){
		int header = readHeader(3);

		//leo el bend
		if(((header & EFFECT_BEND) != 0)){
			effect.setBend(readBendEffect());
		}

		//leo el tremolo bar
		if(((header & EFFECT_TREMOLO_BAR) != 0)){
			effect.setTremoloBar(readTremoloBarEffect());
		}

		//leo el harmonic
		if(((header & EFFECT_HARMONIC) != 0)){
			effect.setHarmonic(readHarmonicEffect());
		}

		//leo el grace
		if(((header & EFFECT_GRACE) != 0)){
			effect.setGrace(readGraceEffect());
		}

		//leo el trill
		if(((header & EFFECT_TRILL) != 0)){
			effect.setTrill(readTrillEffect());
		}

		//leo el tremolo picking
		if(((header & EFFECT_TREMOLO_PICKING) != 0)){
			effect.setTremoloPicking(readTremoloPickingEffect());
		}

		//vibrato
		effect.setVibrato(((header & EFFECT_VIBRATO) != 0));

		//dead note
		effect.setDeadNote(((header & EFFECT_DEAD) != 0));

		//slide
		effect.setSlide(((header & EFFECT_SLIDE) != 0));

		//hammer-on/pull-off
		effect.setHammer(((header & EFFECT_HAMMER) != 0));

		//ghost note
		effect.setGhostNote(((header & EFFECT_GHOST) != 0));

		//accentuated note
		effect.setAccentuatedNote(((header & EFFECT_ACCENTUATED) != 0));

		//heavy accentuated note
		effect.setHeavyAccentuatedNote(((header & EFFECT_HEAVY_ACCENTUATED) != 0));

		//palm mute
		effect.setPalmMute(((header & EFFECT_PALM_MUTE) != 0));

		//staccato
		effect.setStaccato(((header & EFFECT_STACCATO) != 0));

		//tapping
		effect.setTapping(((header & EFFECT_TAPPING) != 0));

		//slapping
		effect.setSlapping(((header & EFFECT_SLAPPING) != 0));

		//popping
		effect.setPopping(((header & EFFECT_POPPING) != 0));

		//fade in
		effect.setFadeIn(((header & EFFECT_FADE_IN) != 0));
	}

	private TGEffectBend readBendEffect(){
		TGEffectBend bend = this.factory.newEffectBend();

		//leo la cantidad de puntos
		int count = readByte();

		for(int i = 0;i < count;i++){
			//leo la posicion
			int position = readByte();

			//leo el valor
			int value = readByte();

			//agrego el punto
			bend.addPoint(position,value);
		}
		return bend;
	}

	private TGEffectTremoloBar readTremoloBarEffect(){
		TGEffectTremoloBar tremoloBar = this.factory.newEffectTremoloBar();

		//leo la cantidad de puntos
		int count = readByte();

		for(int i = 0;i < count;i++){
			//leo la posicion
			int position = readByte();

			//leo el valor
			int value =  (readByte() - TGEffectTremoloBar.MAX_VALUE_LENGTH);

			//agrego el punto
			tremoloBar.addPoint(position,value);
		}
		return tremoloBar;
	}

	private TGEffectHarmonic readHarmonicEffect(){
		TGEffectHarmonic effect = this.factory.newEffectHarmonic();

		//leo el tipo
		effect.setType(readByte());

		//leo la data
		if(effect.getType() != TGEffectHarmonic.TYPE_NATURAL){
			effect.setData(readByte());
		}
		return effect;
	}

	private TGEffectGrace readGraceEffect(){
		int header = readHeader();

		TGEffectGrace effect = this.factory.newEffectGrace();

		effect.setDead((header & GRACE_FLAG_DEAD) != 0) ;

		effect.setOnBeat((header & GRACE_FLAG_ON_BEAT) != 0) ;

		//leo el fret
		effect.setFret(readByte());

		//leo la duracion
		effect.setDuration(readByte());

		//leo el velocity
		effect.setDynamic(readByte());

		//leo la transicion
		effect.setTransition(readByte());

		return effect;
	}

	private TGEffectTremoloPicking readTremoloPickingEffect(){
		TGEffectTremoloPicking effect = this.factory.newEffectTremoloPicking();

		//leo la duracion
		effect.getDuration().setValue(readByte());

		return effect;
	}

	private TGEffectTrill readTrillEffect(){
		TGEffectTrill effect = this.factory.newEffectTrill();

		//leo el fret
		effect.setFret(readByte());

		//leo la duracion
		effect.getDuration().setValue(readByte());

		return effect;
	}

	private TGMarker readMarker(int measure){
		TGMarker marker = this.factory.newMarker();

		marker.setMeasure(measure);

		//leo el titulo
		marker.setTitle(readUnsignedByteString());

		//leo el color
		readRGBColor(marker.getColor());

		return marker;
	}

	private void readRGBColor(TGColor color){
		//leo el RGB
		color.setR((readByte() & 0xff));
		color.setG((readByte() & 0xff));
		color.setB((readByte() & 0xff));
	}

	private void readLyrics(TGLyric lyrics){
		//leo el compas de comienzo
		lyrics.setFrom(readShort());

		//leo el texto
		lyrics.setLyrics(readIntegerString());
	}

	private byte readByte(){
		try {
			return (byte)this.dataInputStream.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int readHeader(){
		try {
			return this.dataInputStream.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int readHeader(int bCount){
		int header = 0;
		for(int i = bCount; i > 0; i --){
			header += ( readHeader() << ( (8 * i) - 8 ) );
		}
		return header;
	}

	private short readShort(){
		try {
			return this.dataInputStream.readShort();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String readUnsignedByteString(){
		try {
			return readString( (this.dataInputStream.read() & 0xFF ));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String readIntegerString(){
		try {
			return readString(this.dataInputStream.readInt());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String readString(int length) throws IOException {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < length; i ++){
			sb.append(this.dataInputStream.readChar());
		}
		return sb.toString();
	}
}
