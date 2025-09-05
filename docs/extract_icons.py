#!/usr/bin/env python3
"""
Simple script to parse Kotlin ImageVector definitions and generate SVG files.
This bypasses the need for Gradle/Compose dependencies that require network access.
"""

import os
import re
import sys
from pathlib import Path

def sanitize_filename(name):
    """Sanitize name for use as filename"""
    return re.sub(r'[^A-Za-z0-9._-]', '_', name)

def extract_path_data(content):
    """Extract path data from Kotlin ImageVector definition"""
    paths = []
    
    # Find ImageVector.Builder calls
    builder_match = re.search(r'ImageVector\.Builder\("([^"]*)",\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', content)
    if not builder_match:
        return None, None, None, None
        
    name = builder_match.group(1)
    # Extract dimensions - just use the viewBox values for now
    width = "26"
    height = "26" 
    viewbox_width = "26"
    viewbox_height = "26"
    
    # Extract path blocks
    path_blocks = re.finditer(r'path\s*\([^{]*\)\s*\{([^}]*)\}', content, re.DOTALL)
    
    for path_block in path_blocks:
        path_content = path_block.group(1)
        
        # Extract fill color
        fill = "#000000"  # Default black
        fill_match = re.search(r'fill\s*=\s*SolidColor\(Color\.(\w+)\)', content)
        if fill_match:
            color_name = fill_match.group(1)
            if color_name == "Black":
                fill = "#000000"
            elif color_name == "White":
                fill = "#ffffff"
            # Add more colors as needed
                
        # Convert path commands
        path_data = convert_path_commands(path_content)
        if path_data:
            paths.append({
                'data': path_data,
                'fill': fill
            })
    
    return name, (width, height, viewbox_width, viewbox_height), paths

def convert_path_commands(content):
    """Convert Kotlin path commands to SVG path data"""
    path_data = []
    
    # Extract path commands using regex
    commands = [
        (r'moveTo\(([^,]+),\s*([^)]+)\)', r'M \1 \2'),
        (r'lineTo\(([^,]+),\s*([^)]+)\)', r'L \1 \2'),
        (r'curveTo\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)', r'C \1 \2 \3 \4 \5 \6'),
        (r'verticalLineTo\(([^)]+)\)', r'V \1'),
        (r'horizontalLineTo\(([^)]+)\)', r'H \1'),
        (r'close\(\)', 'Z')
    ]
    
    lines = content.split('\n')
    path_parts = []
    
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        for pattern, replacement in commands:
            if re.search(pattern, line):
                if isinstance(replacement, str) and replacement == 'Z':
                    path_parts.append('Z')
                else:
                    matches = re.findall(pattern, line)
                    for match in matches:
                        if isinstance(match, tuple):
                            if len(match) == 2:  # moveTo, lineTo, verticalLineTo, horizontalLineTo
                                # Remove 'f' suffix from float literals
                                x = match[0].replace('f', '')
                                y = match[1].replace('f', '')
                                if 'moveTo' in line:
                                    path_parts.append(f'M {x} {y}')
                                elif 'lineTo' in line:
                                    path_parts.append(f'L {x} {y}')
                                elif 'verticalLineTo' in line:
                                    path_parts.append(f'V {x}')
                                elif 'horizontalLineTo' in line:
                                    path_parts.append(f'H {x}')
                            elif len(match) == 6:  # curveTo
                                # Remove 'f' suffix from all coordinates
                                coords = [coord.replace('f', '') for coord in match]
                                path_parts.append(f'C {" ".join(coords)}')
                break
    
    return ' '.join(path_parts) if path_parts else None

def generate_svg(name, dimensions, paths):
    """Generate SVG content"""
    width, height, vw, vh = dimensions
    
    svg_content = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {vw} {vh}" fill="none" aria-label="{name}">
'''
    
    for path in paths:
        svg_content += f'  <path d="{path["data"]}" fill="{path["fill"]}" fill-rule="evenodd"/>\n'
    
    svg_content += '</svg>\n'
    return svg_content

def process_kotlin_file(file_path):
    """Process a single Kotlin file and extract icon data"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extract the icon name from property definition
    prop_match = re.search(r'val\s+MiuixIcons\.[^.]+\.(\w+):', content)
    if not prop_match:
        return None
        
    icon_name = prop_match.group(1)
    
    name, dimensions, paths = extract_path_data(content)
    if not name or not paths:
        print(f"Warning: Could not extract path data from {file_path}")
        return None
        
    return {
        'name': icon_name,
        'svg_name': name or icon_name,
        'dimensions': dimensions,
        'paths': paths
    }

def main():
    """Main function"""
    if len(sys.argv) != 3:
        print("Usage: python3 extract_icons.py <source_dir> <output_dir>")
        sys.exit(1)
        
    source_dir = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])
    
    if not source_dir.exists():
        print(f"Error: Source directory {source_dir} does not exist")
        sys.exit(1)
        
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Find all Kotlin files in the icons directory
    icon_files = list(source_dir.glob('**/*.kt'))
    
    success_count = 0
    fail_count = 0
    
    for file_path in icon_files:
        try:
            icon_data = process_kotlin_file(file_path)
            if icon_data:
                svg_content = generate_svg(
                    icon_data['svg_name'],
                    icon_data['dimensions'],
                    icon_data['paths']
                )
                
                output_file = output_dir / f"{sanitize_filename(icon_data['name'])}.svg"
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(svg_content)
                
                print(f"Generated: {output_file}")
                success_count += 1
            else:
                fail_count += 1
        except Exception as e:
            print(f"Error processing {file_path}: {e}")
            fail_count += 1
    
    print(f"\nDone. Success: {success_count}, Failed: {fail_count}")

if __name__ == '__main__':
    main()