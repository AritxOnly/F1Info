import AppKit
import Vision
import CoreImage
import ImageIO
import UniformTypeIdentifiers
let args = CommandLine.arguments
let input = URL(fileURLWithPath: args[1])
let output = URL(fileURLWithPath: args[2])
let image = CIImage(contentsOf: input)!
let handler = VNImageRequestHandler(url: input, options: [:])
let request = VNGenerateForegroundInstanceMaskRequest()
try handler.perform([request])
guard let result = request.results?.first else { fatalError("No foreground detected") }
var instances = result.allInstances
if args.count > 3 {
    let buffer = result.instanceMask
    CVPixelBufferLockBaseAddress(buffer, .readOnly)
    let x = Int(Double(CVPixelBufferGetWidth(buffer)) * 0.53)
    let y = Int(Double(CVPixelBufferGetHeight(buffer)) * 0.60)
    let stride = CVPixelBufferGetBytesPerRow(buffer)
    let index = Int(CVPixelBufferGetBaseAddress(buffer)!.assumingMemoryBound(to: UInt8.self)[y * stride + x])
    CVPixelBufferUnlockBaseAddress(buffer, .readOnly)
    if index > 0 { instances = IndexSet(integer: index) }
}
let mask = try result.generateScaledMaskForImage(forInstances: instances, from: handler)
let matte = CIImage(cvPixelBuffer: mask)
let empty = CIImage(color: .clear).cropped(to: image.extent)
let cutout = image.applyingFilter("CIBlendWithMask", parameters: [kCIInputBackgroundImageKey: empty, kCIInputMaskImageKey: matte])
let context = CIContext()
let cg = context.createCGImage(cutout, from: image.extent)!
let destination = CGImageDestinationCreateWithURL(output as CFURL, UTType.png.identifier as CFString, 1, nil)!
CGImageDestinationAddImage(destination, cg, nil)
guard CGImageDestinationFinalize(destination) else { fatalError("Save failed") }
print("saved",output.path,"instances",instances)
