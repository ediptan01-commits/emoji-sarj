"""Build a charging-only HONOR test theme with the official alpha encoder.

The editor/encoder is obtained separately from HONOR; it is not redistributed.
Uses the 2.5.0 editor's 8.1 template schema and ordinary charging directory export.
Successful structural validation is not a device compatibility certification.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import json
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

from PIL import Image, ImageDraw, ImageFont


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--frames-root', type=Path, required=True)
    parser.add_argument('--encoder', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    root, encoder, output = args.frames_root.resolve(), args.encoder.resolve(), args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    frames = sorted((root/'frames').glob('frame_*.png'))
    assert len(frames) == 180, 'Run build_frames.py first'
    for frame in frames:
        with Image.open(frame) as im:
            assert im.mode == 'RGBA' and im.size == (1080, 2160)
    video = root/'charging_video.mp4'
    request = dict(inputAudioFile='', inputPngDir=str(root/'frames'), inputPngHeight=2160,
                   inputPngWidth=1080, outputMp4File=str(video), videoFps=30, loopFirstFrame=1)
    encoded = base64.b64encode(json.dumps(request).encode()).decode()
    flags = getattr(subprocess, 'CREATE_NO_WINDOW', 0)
    with (output/'encoder.log').open('wb') as log:
        subprocess.run([str(encoder), encoded], cwd=encoder.parent, stdout=log,
                       stderr=subprocess.STDOUT, check=True, timeout=300, creationflags=flags)
    probe = subprocess.run([str(encoder.with_name('ffprobe.exe')), '-v', 'error', '-show_streams',
                            '-show_format', '-of', 'json', str(video)], capture_output=True,
                           check=True, creationflags=flags)
    metadata = json.loads(probe.stdout)
    streams = metadata['streams']
    assert len(streams) == 2, 'Official alpha stream is missing'
    assert (streams[0]['width'], streams[0]['height']) == (1080, 2160)
    assert streams[0]['nb_frames'] == streams[1]['nb_frames'] == '180'
    assert streams[0]['avg_frame_rate'] == '30/1'
    assert streams[1]['codec_tag_string'] == 'mp4s', 'Expected HONOR alpha data track'
    assert float(metadata['format']['duration']) == 6
    # 8.0.2 is a resource revision, not a claim to run on MagicOS 8 only.
    # HONOR's 8.1 template itself uses the 8.0.1 description version.
    desc = ET.Element('HnTheme')
    fields = dict(title='Pompi charging', designer='Pompi', screen='FHD', version='8.0.2',
                  briefInfo='Pompi pumping and wiping sweat beside the native charging percentage.')
    for key, value in fields.items():
        ET.SubElement(desc, key).text = value
    ET.SubElement(desc, 'extra').text = json.dumps({'extraContentFlag':'1',
        'en':{'title':'Pompi charging','briefInfo':fields['briefInfo']},
        'tr':{'title':'Pompi sarj','briefInfo':'Pompi, sistemin sarj yuzdesinin yaninda pompalar.'}})
    config = ET.Element('ChargingConfig')
    ET.SubElement(config, 'version').text = '2.0.0'
    ET.SubElement(config, 'base_scale', width='1080', height='2160')
    ET.SubElement(config, 'video_framerate', framerate='30')
    ET.SubElement(config, 'background_color').text = '#88000000'
    icon_attrs = dict(x='650', y='885', w='90', h='90')
    ET.SubElement(config, 'lightning_icon', **icon_attrs, path='./lightning.png')
    icons = ET.SubElement(config, 'lightning_icons')
    for level, name in enumerate(('normal','super','turbo')):
        ET.SubElement(icons, f'level{level}', **icon_attrs, path=f'./lightning_{name}.png')
    ET.SubElement(config, 'progress_text', content='94', x='650', y='1065', size='150', color='#ffffffff', path='')
    ET.SubElement(config, 'percentage_text', content='%', x='790', y='1106', x_rel='10', size='50', color='#ffffffff', path='')
    prompts = ET.SubElement(config, 'charging_prompt_texts')
    languages = ET.Element('resources')
    for level, (en, tr) in enumerate(zip(('Charging','SuperCharge','SuperCharge Turbo'),
                                       ('Şarj oluyor','Süper şarj','Süper şarj Turbo'))):
        ET.SubElement(prompts, f'level{level}', content=en, x='650', y='1240', size='36', color='#ccffffff', path='')
        for lang, text in [('en',en),('tr',tr)]:
            ET.SubElement(languages,'string', name=f'level{level}', lang=lang).text = text
    segment = ET.SubElement(config, 'video_segment', path='./', file='charging_video.mp4', alias='')
    clip = ET.SubElement(segment, 'clip_item', tag='Pompi', screen='0x03', orientation='0x00')
    for key, value in [('frame_init_begin',0),('frame_init_end',1),('frame_loop_begin',1),('frame_loop_end',180)]:
        ET.SubElement(clip,key).text = str(value)
    def xml(node):
        ET.indent(node)
        return ET.tostring(node,encoding='utf-8',xml_declaration=True)
    theme = output/'Pompi-MagicOS10-deneme.hnt'
    preview = Image.new('RGBA',(1080,2160),(7,13,30,255))
    preview.alpha_composite(Image.open(frames[60]).convert('RGBA'))
    draw = ImageDraw.Draw(preview)
    # This cover is explicitly a mock; numbers are never put in charging_video.mp4.
    font_path = Path('C:/Windows/Fonts/arial.ttf')
    font = ImageFont.truetype(str(font_path),130) if font_path.exists() else ImageFont.load_default(size=130)
    draw.text((650,1065),'94%',font=font,fill='white',anchor='mm')
    small = ImageFont.truetype(str(font_path),30) if font_path.exists() else ImageFont.load_default(size=30)
    draw.text((540,1600),'YERLESIM ONIZLEMESI - cihaz testi gerekli',font=small,fill='white',anchor='mm')
    preview.convert('RGB').save(output/'honor-pompi-onizleme.jpg', quality=88)
    lightning = Image.new('RGBA',(90,90))
    ImageDraw.Draw(lightning).polygon([(51,3),(18,51),(40,51),(31,87),(73,36),(49,36)],fill='white')
    import io
    icon = io.BytesIO(); lightning.save(icon,format='PNG')
    with ZipFile(theme,'w',ZIP_DEFLATED,compresslevel=9) as archive:
        archive.writestr('description.xml',xml(desc))
        archive.writestr('charging/charging_config.xml',xml(config))
        archive.writestr('charging/contentLang.xml',xml(languages))
        archive.write(video,'charging/charging_video.mp4')
        for name in ['lightning','lightning_normal','lightning_super','lightning_turbo']:
            archive.writestr(f'charging/{name}.png',icon.getvalue())
        archive.write(output/'honor-pompi-onizleme.jpg','preview/cover.jpg')
        archive.write(output/'honor-pompi-onizleme.jpg','preview/preview_charging_0.jpg')
    with ZipFile(theme) as archive:
        assert archive.testzip() is None
        names = archive.namelist()
        assert not any(n.startswith(('unlock/','wallpaper/','icons/','com.')) for n in names)
        for name in names:
            if name.endswith('.xml'): ET.fromstring(archive.read(name))
        for item in config.findall('.//*[@path]'):
            if item.get('path') and item.get('path') != './':
                assert 'charging/'+item.get('path').removeprefix('./') in names
        assert 'charging/'+segment.get('file') in names
    assert theme.stat().st_size < 95*1024*1024
    report = {'status':'structurally validated test candidate; NOT tested on a phone',
              'encoder':'HONOR Theme Editor 2.5.0 AlphaVideoMakeTool',
              'encoderSha256':hashlib.sha256(encoder.read_bytes()).hexdigest(),
              'themeSha256':hashlib.sha256(theme.read_bytes()).hexdigest(),
              'frameCount':180,'durationSeconds':6,'width':1080,'height':2160,
              'officialAlphaTrack':True,'containsPrivateScreenshot':False,'changesOnlyChargingModule':True,
              'device':'HONOR Magic7 Pro, MagicOS 10; regional compatibility unverified',
              'streams':streams}
    (output/'honor-dogrulama.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
    print(json.dumps({'theme':str(theme),'bytes':theme.stat().st_size,'deviceTested':False}))


if __name__ == '__main__':
    main()
