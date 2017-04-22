#!/usr/bin/env python

#
# create_icons.py
#
# Create icons from Font Awesome using Icon Font to PNG
# https://github.com/odyniec/icon-font-to-png
#

import icon_font_to_png
import os

class Drawable:
    'Common base class for all icons'

    def __init__(self, name, filename = '', size = 24, scale = 'auto', color = 'white'):
        self.name = name
        if filename:
            self.filename = filename
        else:
            self.filename = name
        self.filename = filename
        self.size = size
        self.scale = scale
        self.color = color

    def displayDrawable(self):
        print("Name : ", self.name,  ", Size: ", self.size, ", Scale: ", self.scale, ", Color: ", self.color)

print("Create drawables")

drawables = [
    Drawable(name='bitbucket', filename='ab_icon_repositories'),
    Drawable(name='book', filename='ab_icon_wiki'),
    Drawable(name='bug', filename='icon_bug', color='#707070'),
    Drawable(name='check', filename='ab_icon_submit'),
    Drawable(name='check-circle', filename='ic_action_resolve'),
    Drawable(name='code', filename='ab_icon_source'),
    Drawable(name='code-fork', filename='ab_icon_branch'),
    Drawable(name='code-fork', filename='icon_fork', color='#707070'),
    Drawable(name='cog', filename='ab_icon_settings'),
    Drawable(name='comments-o', filename='ab_icon_issues'),
    Drawable(name='download', filename='ab_icon_download'),
    Drawable(name='file-text', filename='icon_file', color='#707070'),
    Drawable(name='folder', filename='icon_folder', color='#707070'),
    Drawable(name='globe', filename='ab_icon_translations'),
    Drawable(name='history', filename='ab_icon_commits'),
    Drawable(name='info-circle', filename='ab_icon_about'),
    Drawable(name='lightbulb-o', filename='icon_proposal', color='#707070'),
    Drawable(name='lock', filename='icon_private', color='#707070'),
    Drawable(name='minus-square', filename='icon_removed', color='#707070'),
    Drawable(name='pencil', filename='ab_icon_edit'),
    Drawable(name='plus-circle', filename='ab_icon_add'),
    Drawable(name='pencil-square', filename='icon_modified', color='#707070'),
    Drawable(name='plus-square', filename='icon_added', color='#707070'),
    Drawable(name='puzzle-piece', filename='icon_enhancement', color='#707070'),
    Drawable(name='refresh', filename='ab_icon_refresh'),
    Drawable(name='rss-square', filename='ab_icon_feed'),
    Drawable(name='search', filename='ab_icon_search'),
    Drawable(name='sort-amount-desc', filename='ab_icon_sort', scale='0.92'),
    Drawable(name='tag', filename='ab_icon_tag'),
    Drawable(name='trophy', filename='ab_icon_milestone'),
    Drawable(name='undo', filename='ic_action_reopen'),
    Drawable(name='users', filename='ab_icon_followers'),
    Drawable(name='wrench', filename='icon_task', color='#707070'),
    Drawable(name='plus', filename='ic_add')
    ]
outdirs = dict([('drawable-mdpi', 1), ('drawable-hdpi', 1.5), ('drawable-xhdpi', 2), ('drawable-xxhdpi', 3)])

icons, common_prefix = icon_font_to_png.load_css("font-awesome.css", True)

for d in drawables:
    for dir, mul in outdirs.items():
        size = int(round(d.size * mul))
        print('{0}/{1}.png {2}px'.format(dir, d.name, size))
        icon_font_to_png.export_icon(icons, d.name, size, dir + os.sep + d.filename + ".png", "fontawesome-webfont.ttf", d.color, d.scale)
