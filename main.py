import os
import glob
import cv2
import numpy as np
import argparse
from concurrent.futures import ProcessPoolExecutor, as_completed
import time

# Improved color shift function
def color_shift_frame(frame, b_shift=0, g_shift=0, r_shift=0):
    B, G, R = cv2.split(frame)
    B = np.clip(B.astype(np.int16) + b_shift, 0, 255).astype(np.uint8)
    G = np.clip(G.astype(np.int16) + g_shift, 0, 255).astype(np.uint8)
    R = np.clip(R.astype(np.int16) + r_shift, 0, 255).astype(np.uint8)
    return cv2.merge([B, G, R])

# Improved rotation function
def rotate_frame(frame, angle):
    (h, w) = frame.shape[:2]
    cos = np.abs(np.cos(np.radians(angle)))
    sin = np.abs(np.sin(np.radians(angle)))
    new_w = int((w * cos) + (h * sin))
    new_h = int((h * cos) + (w * sin))
    M = cv2.getRotationMatrix2D((w/2, h/2), angle, 1.0)
    M[0, 2] += (new_w - w) / 2
    M[1, 2] += (new_h - h) / 2
    return cv2.warpAffine(frame, M, (new_w, new_h))

# Brightness adjustment
def adjust_brightness(frame, factor):
    return cv2.convertScaleAbs(frame, alpha=factor, beta=0)

# Frame flipping
def flip_frame(frame, flip_code):
    return cv2.flip(frame, flip_code)

# Gaussian noise addition
def add_gaussian_noise(frame, mean=0, std=15):
    gaussian = np.random.normal(mean, std, frame.shape).astype(np.int16)
    noisy_frame = frame.astype(np.int16) + gaussian
    return np.clip(noisy_frame, 0, 255).astype(np.uint8)

# Main video processing function
def process_video(input_path, output_path, angle, brightness, flip, gaussian, color_shift):
    cap = cv2.VideoCapture(input_path)
    if not cap.isOpened():
        print(f"Error opening video file: {input_path}")
        return

    fps = cap.get(cv2.CAP_PROP_FPS)
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

    # Read the first frame to determine output dimensions after transformation.
    ret, first_frame = cap.read()
    if not ret:
        print("Error reading the first frame.")
        return

    # Transform the first frame to get proper dimensions.
    transformed_first = rotate_frame(first_frame, angle)
    # (Optionally, apply brightness/flip/noise/color_shift here if they change size.)
    out_width = transformed_first.shape[1]
    out_height = transformed_first.shape[0]

    # Create the VideoWriter with the new dimensions.
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(output_path, fourcc, fps, (out_width, out_height))
    
    # Reset the capture back to the start.
    cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
    
    current_frame = 0
    while True:
        ret, frame = cap.read()
        if not ret:
            break

        transformed = rotate_frame(frame, angle)
        if brightness != 1.0:
            transformed = adjust_brightness(transformed, brightness)
        if flip is not None:
            transformed = flip_frame(transformed, flip)
        if gaussian > 0:
            transformed = add_gaussian_noise(transformed, std=gaussian)
        if any(color_shift):
            transformed = color_shift_frame(transformed, *color_shift)

        # Ensure transformed frame matches the chosen output size.
        if (transformed.shape[1], transformed.shape[0]) != (out_width, out_height):
            transformed = cv2.resize(transformed, (out_width, out_height))
            
        out.write(transformed)
        
        current_frame += 1
        if current_frame % 10 == 0:
            print(f"Processing: {current_frame/total_frames:.1%}")

    cap.release()
    out.release()
    print(f"Transformed video saved to: {output_path}")

# Folder processing with multiprocessing
def process_videos_in_folder(input_folder, output_folder, angle, brightness, flip, gaussian, color_shift):
    avi_files = glob.glob(os.path.join(input_folder, "**", "*.avi"), recursive=True) #list of all avi files
    avi_files += glob.glob(os.path.join(input_folder, "**", "*.AVI"), recursive=True)
    print(avi_files)
    if not avi_files:
        print(f"No AVI files found in {input_folder}")
        return

    os.makedirs(output_folder, exist_ok=True)

    with ProcessPoolExecutor() as executor:
        futures = []
        for avi_file in avi_files:
            filename = os.path.basename(avi_file)
            out_path = os.path.join(output_folder, filename)
            futures.append(executor.submit(
                process_video, avi_file, out_path, angle, brightness, flip, gaussian, color_shift
            ))

        for future in as_completed(futures):
            future.result()

# Argument parsing
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Apply geometric and visual transformations to .avi video files.")
    parser.add_argument("--input", help="Path to input .avi file")
    parser.add_argument("--inputfolder", help="Path to input folder containing .avi files")
    parser.add_argument("--output", help="Name or path for the output .avi file or output folder when processing a folder")
    parser.add_argument("--angle", type=float, default=0, help="Rotation angle in degrees (default: 45)")
    parser.add_argument("--brightness", type=float, default=1.0, help="Brightness factor (default: 1.0, no change)")
    parser.add_argument("--flip", type=int, choices=[-1, 0, 1], help="Flip code: 1 (horizontal), 0 (vertical), -1 (both). If not provided, no flip is applied")
    parser.add_argument("--gaussian", type=float, default=0.0, help="Standard deviation for Gaussian noise (default: 0, no noise)")
    parser.add_argument("--color_shift", nargs=3, type=int, default=[0, 0, 0], help="Color shift for B G R channels (default: 0 0 0, no shift)")
    parser.add_argument("--codec", default="XVID", help="Video codec (default: XVID)")
    
    args = parser.parse_args()

    # Validate inputs
    if not args.inputfolder and not args.input:
        print("Error: You must provide --input for a single file or --inputfolder for a folder of AVI files.")
        exit(1)
    
    # If output argument is just a filename, join it with the current working directory.
    output_path = args.output
    if output_path and not os.path.dirname(output_path):
        output_path = os.path.join(os.getcwd(), output_path)

    # Process videos in either file mode or folder mode
    start_time = time.time()
    if args.inputfolder:
        # For folder processing, if output_path is a filename, treat it as a folder name.
        process_videos_in_folder(args.inputfolder, output_path, args.angle, args.brightness, args.flip, args.gaussian, args.color_shift)
    else:
        process_video(args.input, output_path, args.angle, args.brightness, args.flip, args.gaussian, args.color_shift)
    
    print(f"Total processing time: {time.time() - start_time:.2f} seconds")