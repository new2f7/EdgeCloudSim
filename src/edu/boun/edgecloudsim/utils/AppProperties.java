package edu.boun.edgecloudsim.utils;

public class AppProperties {
	private String name;
	private int videoXSize;
	private int videoYSize;
	private int bitsPerPixel;
	private double fps;
	private double compressionFactor;
	private int instructionsPerPixel;
	private int bitsPerSecond;
	private int instructionsPerFrame;
	private int downloadSize;

	public AppProperties(String _name,
			int _videoXSize,
			int _videoYSize,
			int _bitsPerPixel,
			double _fps,
			double _compressionFactor,
			int _instructionsPerPixel,
			int _downloadSize) {
		name = _name;
		videoXSize = _videoXSize;
		videoYSize = _videoYSize;
		bitsPerPixel = _bitsPerPixel;
		fps = _fps;
		compressionFactor = _compressionFactor;
		instructionsPerPixel = _instructionsPerPixel;
		bitsPerSecond = (int) Math.ceil(videoXSize * videoYSize * bitsPerPixel * fps * compressionFactor);
		instructionsPerFrame = videoXSize * videoYSize * instructionsPerPixel;
		downloadSize = _downloadSize;
	}

	public String getName() {
		return name;
	}

	public int getVideoXSize() {
		return videoXSize;
	}

	public int getVideoYSize() {
		return videoYSize;
	}

	public int getBitsPerPixel() {
		return bitsPerPixel;
	}

	public double getFps() {
		return fps;
	}

	public double getCompressionFactor() {
		return compressionFactor;
	}

	public int getInstructionsPerPixel() {
		return instructionsPerPixel;
	}

	public int getBitsPerSecond() {
		return bitsPerSecond;
	}

	public int getDownloadSize() {
		return downloadSize;
	}

	public int getInstructionsPerFrame() {
		return instructionsPerFrame;
	}

}
