
# DataAugmentor
Note: Step (automated augmentation) isnt complete, neither is CutMix.

## Features
A Python based, command line project to augment video files with a Java Swing GUI. Currently can do:
1. Angle.
2. Brightness.
3. Flip.
4. Gaussian Noise.
5. Color shift (RGB).

In total 6C1 + 6C2 + 6C3... = 63 augmentations on one frame. Thus 63 videos from 1 video.

Multithreaded folder batch processing. 

Mostly use CV2 for augmentations, and argparse for command line capability.
## Requirements
Python 3.12.4+
Java 23.0.1 2024-10-15
Java(TM) SE Runtime Environment (build 23.0.1+11-39)
Java HotSpot(TM) 64-Bit Server VM (build 23.0.1+11-39, mixed mode, sharing)

Developed and ran on Windows 10, not tested on other OSes.
## Setup
	1. Download and unzip this repo.
## How to run
### GUI
Open VideoProcessorGUI.java in any IDE, run it. Select if single file or folder of files to be augmented, and all desired augmentations. Click process once satisfied.
### Command Line
Single file:

```bash python main.py --input input.avi --output output.avi --angle 30 --brightness 1.2 --gaussian 10```
  

Folder:


```bash python main.py --inputfolder input_folder --output output_folder --angle 45 --flip 1```

## Future TODOs
1. Implement Cutout and CutMix.
2. CUDA version (i think only have to update cv2 dependency to cv2-cuda).
3. In total 6C1 + 6C2 + 6C3... = 63 augmentations on one frame. Thus 63 videos from 1 video. Try doing an augmentation on each frame, so 63 * 60 FPS videos.
   1. Investigate which augemenations are useful and which arent that impactful.
   2. Add GUI and steps (Automating augmentation).
4. Possibly GAN (model based) augmentations.
